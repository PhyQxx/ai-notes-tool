package com.ainotes.service;

import com.ainotes.entity.Canvas;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface CanvasService extends IService<Canvas> {
    List<Canvas> listUserCanvases(Long userId);
    Canvas createCanvas(Long userId, String title);
    void updateCanvasData(Long userId, Long canvasId, String data);
}
