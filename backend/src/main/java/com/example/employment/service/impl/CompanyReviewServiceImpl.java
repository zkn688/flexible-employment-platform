package com.example.employment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.employment.entity.CompanyReview;
import com.example.employment.mapper.CompanyReviewMapper;
import com.example.employment.service.CompanyReviewService;
import org.springframework.stereotype.Service;

@Service
public class CompanyReviewServiceImpl extends ServiceImpl<CompanyReviewMapper, CompanyReview> implements CompanyReviewService {
}
