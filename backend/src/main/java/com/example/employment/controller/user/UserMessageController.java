package com.example.employment.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.employment.common.Result;
import com.example.employment.entity.Message;
import com.example.employment.service.MessageService;
import com.example.employment.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/user/messages")
public class UserMessageController {
    private final MessageService messageService;

    public UserMessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public Result<List<Message>> list(HttpServletRequest servletRequest) {
        Long userId = TokenUtils.getUserId(servletRequest);
        return Result.success(messageService.list(new LambdaQueryWrapper<Message>()
                .eq(Message::getReceiverType, 1)
                .eq(Message::getReceiverId, userId)
                .eq(Message::getStatus, 1)
                .orderByDesc(Message::getCreateTime)));
    }

    @PostMapping
    public Result<Message> send(HttpServletRequest servletRequest, @RequestBody Message request) {
        Long userId = TokenUtils.getUserId(servletRequest);
        request.setId(null);
        request.setSenderType(1);
        request.setSenderId(userId);
        request.setIsRead(0);
        request.setStatus(1);
        messageService.save(request);
        return Result.success("发送成功", request);
    }

    @PutMapping("/{id}/read")
    public Result<Void> read(HttpServletRequest servletRequest, @PathVariable Long id) {
        Long userId = TokenUtils.getUserId(servletRequest);
        Message message = messageService.getById(id);
        if (message == null || message.getReceiverType() != 1 || !message.getReceiverId().equals(userId)) {
            throw new IllegalArgumentException("消息不存在");
        }
        Message update = new Message();
        update.setId(id);
        update.setIsRead(1);
        messageService.updateById(update);
        return Result.success("已标记为已读", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest servletRequest, @PathVariable Long id) {
        Long userId = TokenUtils.getUserId(servletRequest);
        Message message = messageService.getById(id);
        if (message == null || message.getReceiverType() != 1 || !message.getReceiverId().equals(userId)) {
            throw new IllegalArgumentException("消息不存在");
        }
        Message update = new Message();
        update.setId(id);
        update.setStatus(0);
        messageService.updateById(update);
        return Result.success("删除成功", null);
    }
}
