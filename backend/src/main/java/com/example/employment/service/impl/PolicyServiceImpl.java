package com.example.employment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.employment.entity.Policy;
import com.example.employment.mapper.PolicyMapper;
import com.example.employment.service.PolicyService;
import org.springframework.stereotype.Service;

@Service
public class PolicyServiceImpl extends ServiceImpl<PolicyMapper, Policy> implements PolicyService {
}
