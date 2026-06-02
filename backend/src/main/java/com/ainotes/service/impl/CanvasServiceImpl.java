package com.ainotes.service.impl;

import com.ainotes.entity.Canvas;
import com.ainotes.mapper.CanvasMapper;
import com.ainotes.service.CanvasService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CanvasServiceImpl extends ServiceImpl<CanvasMapper, Canvas> implements CanvasService {

    @Override
    public List<Canvas> listUserCanvases(Long userId) {
        return list(new LambdaQueryWrapper<Canvas>()
                .eq(Canvas::getUserId, userId)
                .orderByDesc(Canvas::getUpdatedAt));
    }

    @Override
    public Canvas createCanvas(Long userId, String title) {
        Canvas canvas = new Canvas();
        canvas.setUserId(userId);
        canvas.setTitle(title);
        canvas.setData("{\"nodes\":[],\"edges\":[]}");
        save(canvas);
        return canvas;
    }

    @Override
    public void updateCanvasData(Long userId, Long canvasId, String data) {
        update(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Canvas>()
                .eq(Canvas::getId, canvasId)
                .eq(Canvas::getUserId, userId)
                .set(Canvas::getData, data));
    }
}
