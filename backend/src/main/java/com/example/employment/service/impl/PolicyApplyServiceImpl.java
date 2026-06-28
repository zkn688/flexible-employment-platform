package com.example.employment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.employment.entity.PolicyApply;
import com.example.employment.mapper.PolicyApplyMapper;
import com.example.employment.service.PolicyApplyService;
import org.springframework.stereotype.Service;

@Service
public class PolicyApplyServiceImpl extends ServiceImpl<PolicyApplyMapper, PolicyApply> implements PolicyApplyService {
}
