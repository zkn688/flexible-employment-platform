package com.example.employment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.employment.entity.SocialPaymentRecord;
import com.example.employment.mapper.SocialPaymentRecordMapper;
import com.example.employment.service.SocialPaymentRecordService;
import org.springframework.stereotype.Service;

@Service
public class SocialPaymentRecordServiceImpl extends ServiceImpl<SocialPaymentRecordMapper, SocialPaymentRecord> implements SocialPaymentRecordService {
}
