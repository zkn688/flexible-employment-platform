package com.example.employment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.employment.entity.LaborContract;
import com.example.employment.mapper.LaborContractMapper;
import com.example.employment.service.LaborContractService;
import org.springframework.stereotype.Service;

@Service
public class LaborContractServiceImpl extends ServiceImpl<LaborContractMapper, LaborContract> implements LaborContractService {
}
