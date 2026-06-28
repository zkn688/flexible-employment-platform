package com.example.employment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.employment.entity.EmploymentPref;
import com.example.employment.mapper.EmploymentPrefMapper;
import com.example.employment.service.EmploymentPrefService;
import org.springframework.stereotype.Service;

@Service
public class EmploymentPrefServiceImpl extends ServiceImpl<EmploymentPrefMapper, EmploymentPref> implements EmploymentPrefService {
}
