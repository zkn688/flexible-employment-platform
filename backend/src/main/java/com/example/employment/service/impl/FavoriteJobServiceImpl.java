package com.example.employment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.employment.entity.FavoriteJob;
import com.example.employment.mapper.FavoriteJobMapper;
import com.example.employment.service.FavoriteJobService;
import org.springframework.stereotype.Service;

@Service
public class FavoriteJobServiceImpl extends ServiceImpl<FavoriteJobMapper, FavoriteJob> implements FavoriteJobService {
}
