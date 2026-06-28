const fs = require('fs');
const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  Header, Footer, AlignmentType, LevelFormat,
  HeadingLevel, BorderStyle, WidthType, ShadingType,
  PageNumber, PageBreak, TableOfContents
} = require('docx');

// ===== Read markdown =====
const mdPath = 'E:\\keshe\\docs\\用户端软件开发过程报告.md';
const mdContent = fs.readFileSync(mdPath, 'utf-8');
const lines = mdContent.split(/\r?\n/);

// ===== Style constants (matching template) =====
const FONT_ASCII = "Times New Roman";
const FONT_EAST = "宋体";
const FONT_H2_ASCII = "Arial";
const FONT_H2_EAST = "黑体";
const FONT_CODE = "Courier New";

// ===== Helper functions =====
function makeRun(text, opts = {}) {
  const runOpts = {
    font: { ascii: opts.fontAscii || FONT_ASCII, eastAsia: opts.fontEast || FONT_EAST, hAnsi: opts.fontAscii || FONT_ASCII },
    size: opts.size || 21,
    bold: opts.bold || false,
  };
  if (opts.color) runOpts.color = opts.color;
  if (opts.italics) runOpts.italics = true;
  return new TextRun({ text, ...runOpts });
}

function parseInlineFormatting(text) {
  // Handle **bold** and `code` in inline text
  const parts = [];
  let remaining = text;
  while (remaining.length > 0) {
    const boldMatch = remaining.match(/^(.*?)\*\*(.+?)\*\*/s);
    const codeMatch = remaining.match(/^(.*?)`([^`]+)`/s);

    if (boldMatch && (!codeMatch || boldMatch.index <= codeMatch.index)) {
      if (boldMatch[1]) parts.push({ text: boldMatch[1], bold: false, code: false });
      parts.push({ text: boldMatch[2], bold: true, code: false });
      remaining = remaining.slice(boldMatch[0].length);
    } else if (codeMatch) {
      if (codeMatch[1]) parts.push({ text: codeMatch[1], bold: false, code: false });
      parts.push({ text: codeMatch[2], bold: false, code: true });
      remaining = remaining.slice(codeMatch[0].length);
    } else {
      parts.push({ text: remaining, bold: false, code: false });
      remaining = '';
    }
  }
  if (parts.length === 0) parts.push({ text, bold: false, code: false });
  return parts;
}

function makeParagraphWithInlines(text, opts = {}) {
  const parts = parseInlineFormatting(text);
  const runs = parts.map(p => {
    if (p.code) {
      return new TextRun({
        text: p.text,
        font: { ascii: FONT_CODE, eastAsia: FONT_CODE, hAnsi: FONT_CODE },
        size: opts.size || 21,
        bold: p.bold,
      });
    }
    return makeRun(p.text, { ...opts, bold: p.bold });
  });
  return new Paragraph({
    children: runs,
    ...opts.paragraphOpts,
  });
}

// ===== Parse markdown into structured items =====
const items = [];
let i = 0;

while (i < lines.length) {
  const line = lines[i];

  // Code block
  if (line.startsWith('```')) {
    const codeLines = [];
    const lang = line.slice(3).trim();
    i++;
    while (i < lines.length && !lines[i].startsWith('```')) {
      codeLines.push(lines[i]);
      i++;
    }
    i++; // skip closing ```
    items.push({ type: 'code', lang, content: codeLines.join('\n') });
    continue;
  }

  // Table detection: line starts with | and has table separator pattern
  if (line.startsWith('|') && line.endsWith('|') && i + 1 < lines.length &&
      lines[i + 1].match(/^\|[\s\-:|]+\|$/)) {
    const headerLine = line;
    const sepLine = lines[i + 1];
    const dataLines = [];
    i += 2;
    while (i < lines.length && lines[i].startsWith('|') && lines[i].endsWith('|')) {
      dataLines.push(lines[i]);
      i++;
    }
    items.push({ type: 'table', header: headerLine, data: dataLines });
    continue;
  }

  // Headings - Use Chinese section numbering pattern
  if (line.startsWith('# ') && i === 0) {
    // Document title
    items.push({ type: 'title', text: line.slice(2).trim() });
  } else if (line.match(/^## 一、/)) {
    items.push({ type: 'h1', text: line.slice(3).trim() });
  } else if (line.match(/^## [二三四五六七八九十]、/)) {
    items.push({ type: 'h1', text: line.slice(3).trim() });
  } else if (line.startsWith('## ')) {
    // Other ## headings - treat as h2
    items.push({ type: 'h2', text: line.slice(3).trim() });
  } else if (line.startsWith('### ')) {
    items.push({ type: 'h2', text: line.slice(4).trim() });
  } else if (line.startsWith('#### ')) {
    items.push({ type: 'h3', text: line.slice(5).trim() });
  } else if (line.trim() === '') {
    items.push({ type: 'blank' });
  } else {
    // Regular paragraph (might be numbered list item)
    items.push({ type: 'paragraph', text: line });
  }

  i++;
}

// ===== Build document content =====
const pageBorder = { style: BorderStyle.SINGLE, size: 4, color: "000000" };
const tableBorders = { top: pageBorder, bottom: pageBorder, left: pageBorder, right: pageBorder };

function createTableElement(header, data) {
  const headerCells = header.split('|').filter(s => s.trim()).map(s => s.trim());
  const allData = data.map(row => row.split('|').filter(s => s.trim()).map(s => s.trim()));

  const colCount = headerCells.length;
  const contentWidth = 11906 - 1797 * 2; // A4 minus margins
  const colWidth = Math.floor(contentWidth / colCount);

  const rows = [];
  // Header row
  rows.push(new TableRow({
    children: headerCells.map(text =>
      new TableCell({
        borders: tableBorders,
        width: { size: colWidth, type: WidthType.DXA },
        shading: { fill: "D9E2F3", type: ShadingType.CLEAR },
        margins: { top: 80, bottom: 80, left: 120, right: 120 },
        children: [new Paragraph({
          children: [makeRun(text, { bold: true, size: 21 })],
          alignment: AlignmentType.CENTER,
        })],
      })
    ),
  }));

  // Data rows
  for (const rowData of allData) {
    rows.push(new TableRow({
      children: rowData.map(text =>
        new TableCell({
          borders: tableBorders,
          width: { size: colWidth, type: WidthType.DXA },
          margins: { top: 60, bottom: 60, left: 120, right: 120 },
          children: [makeParagraphWithInlines(text, { size: 21 })],
        })
      ),
    }));
    // Pad if row has fewer cells
    while (rows[rows.length - 1].options.children.length < colCount) {
      rows[rows.length - 1].options.children.push(
        new TableCell({
          borders: tableBorders,
          width: { size: colWidth, type: WidthType.DXA },
          margins: { top: 60, bottom: 60, left: 120, right: 120 },
          children: [new Paragraph({ children: [] })],
        })
      );
    }
  }

  return new Table({
    width: { size: contentWidth, type: WidthType.DXA },
    columnWidths: Array(colCount).fill(colWidth),
    rows,
  });
}

// Build paragraphs array
const bodyChildren = [];

// Title page content
// Title
bodyChildren.push(new Paragraph({ spacing: { before: 2400 } }));
bodyChildren.push(new Paragraph({
  children: [new TextRun({
    text: '计算机学院专业综合设计报告',
    font: { ascii: FONT_EAST, eastAsia: FONT_EAST },
    size: 36, bold: true,
  })],
  alignment: AlignmentType.CENTER,
  spacing: { after: 400 },
}));

bodyChildren.push(new Paragraph({
  children: [new TextRun({
    text: '设计项目名称',
    font: { ascii: "黑体", eastAsia: "黑体" },
    size: 36, bold: true,
  })],
  alignment: AlignmentType.CENTER,
  spacing: { after: 200 },
}));

bodyChildren.push(new Paragraph({
  children: [
    new TextRun({ text: '—— ', size: 32, bold: true }),
    new TextRun({ text: '某市灵活就业管理服务平台用户端', font: { ascii: FONT_EAST, eastAsia: FONT_EAST }, size: 32, bold: true }),
  ],
  alignment: AlignmentType.CENTER,
  spacing: { after: 600 },
}));

// Info table on title page
const infoTableWidth = 5517; // ~ approx width
const infoCol1 = 1332;
const infoCol2 = 4185;
const bottomOnly = { bottom: { style: BorderStyle.SINGLE, size: 4, color: "000000" } };
const noBorder = { bottom: { style: BorderStyle.NONE } };

const infoFields = [
  { label: '姓   名', value: '' },
  { label: '班   级', value: '' },
  { label: '学   号', value: '' },
  { label: '学科专业', value: '计算机科学与技术' },
  { label: '同组成员', value: '' },
  { label: '指导教师', value: '' },
  { label: '日   期', value: '年   月   日' },
];

const infoRows = infoFields.map((f, idx) => {
  const isLast = idx === infoFields.length - 1;
  const cellBorder = (idx === 0) ? noBorder : { top: { style: BorderStyle.SINGLE, size: 4, color: "000000" }, bottom: isLast ? { style: BorderStyle.SINGLE, size: 4, color: "000000" } : { style: BorderStyle.SINGLE, size: 4, color: "000000" } };
  return new TableRow({
    children: [
      new TableCell({
        borders: idx === 0 ? { bottom: { style: BorderStyle.NONE } } : { bottom: isLast ? { style: BorderStyle.SINGLE, size: 4, color: "000000" } : { style: BorderStyle.NONE } },
        width: { size: infoCol1, type: WidthType.DXA },
        margins: { top: 60, bottom: 60, left: 120, right: 120 },
        verticalAlign: "center",
        children: [new Paragraph({
          children: [makeRun(f.label, { size: 21 })],
          alignment: AlignmentType.LEFT,
        })],
      }),
      new TableCell({
        borders: cellBorder,
        width: { size: infoCol2, type: WidthType.DXA },
        margins: { top: 60, bottom: 60, left: 120, right: 120 },
        verticalAlign: "center",
        children: [new Paragraph({
          children: [makeRun(f.value, { size: 21 })],
        })],
      }),
    ],
  });
});

bodyChildren.push(new Table({
  width: { size: infoTableWidth, type: WidthType.DXA },
  columnWidths: [infoCol1, infoCol2],
  rows: infoRows,
  alignment: AlignmentType.CENTER,
}));

bodyChildren.push(new Paragraph({ spacing: { before: 1200 } }));
bodyChildren.push(new Paragraph({
  children: [makeRun('中原工学院计算机学院', { size: 36, bold: true, fontAscii: FONT_EAST, fontEast: FONT_EAST })],
  alignment: AlignmentType.CENTER,
}));

// Page break - TOC page
bodyChildren.push(new Paragraph({ children: [new PageBreak()] }));

// TOC heading
bodyChildren.push(new Paragraph({
  children: [new TextRun({
    text: '目  录',
    font: { ascii: FONT_EAST, eastAsia: FONT_EAST },
    size: 32, bold: true,
  })],
  alignment: AlignmentType.CENTER,
  spacing: { after: 300 },
}));

// TOC field
bodyChildren.push(new TableOfContents("Table of Contents", {
  hyperlink: true,
  headingStyleRange: "1-3",
}));

// Page break before content
bodyChildren.push(new Paragraph({ children: [new PageBreak()] }));

// ===== Process markdown items into content =====
for (let idx = 0; idx < items.length; idx++) {
  const item = items[idx];

  if (item.type === 'blank') {
    bodyChildren.push(new Paragraph({ spacing: { before: 60, after: 60 } }));
    continue;
  }

  if (item.type === 'title') {
    // Document title on first page - already handled
    continue;
  }

  if (item.type === 'h1') {
    // Heading 1: Times New Roman/宋体, 12pt (sz=24), bold
    bodyChildren.push(new Paragraph({
      heading: HeadingLevel.HEADING_1,
      children: [makeRun(item.text, { size: 24, bold: true, fontAscii: FONT_ASCII, fontEast: FONT_EAST })],
      spacing: { before: 340, after: 330 },
    }));
    continue;
  }

  if (item.type === 'h2') {
    // Heading 2: Arial/黑体, 16pt (sz=32), bold
    bodyChildren.push(new Paragraph({
      heading: HeadingLevel.HEADING_2,
      children: [makeRun(item.text, { size: 32, bold: true, fontAscii: FONT_H2_ASCII, fontEast: FONT_H2_EAST })],
      spacing: { before: 260, after: 260 },
    }));
    continue;
  }

  if (item.type === 'h3') {
    // Heading 3: Times New Roman/宋体, 16pt (sz=32), bold
    bodyChildren.push(new Paragraph({
      heading: HeadingLevel.HEADING_3,
      children: [makeRun(item.text, { size: 32, bold: true })],
      spacing: { before: 260, after: 260 },
    }));
    continue;
  }

  if (item.type === 'code') {
    // Code block
    const codeLines = item.content.split('\n');
    for (const cl of codeLines) {
      bodyChildren.push(new Paragraph({
        children: [new TextRun({
          text: cl || ' ',
          font: { ascii: FONT_CODE, eastAsia: FONT_CODE, hAnsi: FONT_CODE },
          size: 18,
        })],
        spacing: { before: 0, after: 0, line: 280, lineRule: 'exact' },
        indent: { left: 720 },
      }));
    }
    // Add small spacing after code block
    bodyChildren.push(new Paragraph({ spacing: { before: 60, after: 60 } }));
    continue;
  }

  if (item.type === 'table') {
    bodyChildren.push(new Paragraph({ spacing: { before: 120 } }));
    bodyChildren.push(createTableElement(item.header, item.data));
    bodyChildren.push(new Paragraph({ spacing: { after: 120 } }));
    continue;
  }

  if (item.type === 'paragraph') {
    let text = item.text.trim();
    if (!text) {
      bodyChildren.push(new Paragraph({ spacing: { before: 60, after: 60 } }));
      continue;
    }

    // Check if it's a numbered list item
    const numMatch = text.match(/^(\d+)\.\s+(.+)/);
    if (numMatch) {
      bodyChildren.push(new Paragraph({
        children: [makeRun(text, { size: 24 })],
        indent: { left: 720, hanging: 360 },
        spacing: { before: 40, after: 40, line: 360 },
      }));
      continue;
    }

    // Check if it's a "- " list item
    if (text.startsWith('- ') || text.startsWith('├─ ') || text.startsWith('│  ') || text.startsWith('└─ ')) {
      bodyChildren.push(new Paragraph({
        children: [makeRun(text, { size: 24 })],
        indent: { left: 720 },
        spacing: { before: 20, after: 20, line: 340 },
      }));
      continue;
    }

    // Check for "text ↓" flow diagram patterns
    if (text === '↓') {
      bodyChildren.push(new Paragraph({
        children: [makeRun('↓', { size: 24 })],
        alignment: AlignmentType.CENTER,
        spacing: { before: 20, after: 20 },
      }));
      continue;
    }

    // Regular body text with first-line indent
    bodyChildren.push(new Paragraph({
      children: [makeRun(text, { size: 24, fontAscii: FONT_ASCII, fontEast: FONT_EAST })],
      indent: { firstLine: 720 },
      spacing: { after: 120, line: 360, lineRule: 'exact' },
    }));
  }
}

// ===== Create document =====
const doc = new Document({
  styles: {
    default: {
      document: {
        run: { font: FONT_ASCII, size: 21 },
      },
    },
    paragraphStyles: [
      {
        id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 24, bold: true, font: FONT_ASCII },
        paragraph: {
          spacing: { before: 340, after: 330 },
          outlineLevel: 0,
        },
      },
      {
        id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 32, bold: true, font: FONT_H2_ASCII },
        paragraph: {
          spacing: { before: 260, after: 260 },
          outlineLevel: 1,
        },
      },
      {
        id: "Heading3", name: "Heading 3", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 32, bold: true, font: FONT_ASCII },
        paragraph: {
          spacing: { before: 260, after: 260 },
          outlineLevel: 2,
        },
      },
    ],
  },
  sections: [
    {
      properties: {
        page: {
          size: { width: 11906, height: 16838 },
          margin: { top: 1440, right: 1797, bottom: 1440, left: 1797 },
        },
      },
      headers: {
        default: new Header({
          children: [new Paragraph({
            children: [new TextRun({
              text: '中原工学院计算机学院专业综合设计报告',
              font: { ascii: FONT_EAST, eastAsia: FONT_EAST },
              size: 18,
            })],
            alignment: AlignmentType.CENTER,
            border: { bottom: { style: BorderStyle.SINGLE, size: 6, color: "auto", space: 1 } },
          })],
        }),
      },
      footers: {
        default: new Footer({
          children: [new Paragraph({
            children: [
              new TextRun({ text: '— ', size: 18 }),
              new TextRun({ children: [PageNumber.CURRENT], size: 18 }),
              new TextRun({ text: ' —', size: 18 }),
            ],
            alignment: AlignmentType.CENTER,
          })],
        }),
      },
      children: bodyChildren,
    },
  ],
});

// ===== Write file =====
const outPath = 'E:\\keshe\\docs\\用户端软件开发过程报告.docx';
Packer.toBuffer(doc).then(buffer => {
  fs.writeFileSync(outPath, buffer);
  console.log(`Document created: ${outPath}`);
}).catch(err => {
  console.error('Error:', err.message);
  process.exit(1);
});
