package com.example.employment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.employment.entity.Resume;
import com.example.employment.mapper.ResumeMapper;
import com.example.employment.service.ResumeService;
import org.springframework.stereotype.Service;

@Service
public class ResumeServiceImpl extends ServiceImpl<ResumeMapper, Resume> implements ResumeService {
}
