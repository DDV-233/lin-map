package com.campus.nav.view;

import com.campus.nav.model.Location;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 地图面板（占位类，将在后续完善）
 */
public class MapPanel extends JPanel {
    private List<Location> locations = new ArrayList<>();
    private List<Location> pathLocations = new ArrayList<>();
    private Location selectedStartLocation;
    private Location selectedEndLocation;
    private Point mousePosition;
    
    public MapPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 绘制网格背景
        drawGrid(g2d);
        
        // 绘制路径
        drawPath(g2d);
        
        // 绘制地点
        drawLocations(g2d);
        
        // 绘制选中状态
        drawSelectedLocations(g2d);
        
        // 绘制鼠标位置
        drawMousePosition(g2d);
    }
    
    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(240, 240, 240));
        
        int gridSize = 20;
        int width = getWidth();
        int height = getHeight();
        
        // 绘制垂直线
        for (int x = 0; x < width; x += gridSize) {
            g2d.drawLine(x, 0, x, height);
        }
        
        // 绘制水平线
        for (int y = 0; y < height; y += gridSize) {
            g2d.drawLine(0, y, width, y);
        }
    }
    
    private void drawPath(Graphics2D g2d) {
        if (pathLocations.size() < 2) return;
        
        g2d.setColor(new Color(0, 123, 255, 200));
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        Location prev = null;
        for (Location location : pathLocations) {
            if (prev != null) {
                int x1 = (int) prev.getXCoordinate().doubleValue();
                int y1 = (int) prev.getYCoordinate().doubleValue();
                int x2 = (int) location.getXCoordinate().doubleValue();
                int y2 = (int) location.getYCoordinate().doubleValue();
                
                g2d.drawLine(x1, y1, x2, y2);
            }
            prev = location;
        }
    }
    
    private void drawLocations(Graphics2D g2d) {
        for (Location location : locations) {
            int x = (int) location.getXCoordinate().doubleValue();
            int y = (int) location.getYCoordinate().doubleValue();
            
            // 根据类型设置颜色
            Color color = getLocationColor(location);
            g2d.setColor(color);
            
            // 绘制圆形
            g2d.fillOval(x - 8, y - 8, 16, 16);
            
            // 绘制边框
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x - 8, y - 8, 16, 16);
            
            // 绘制名称
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
            g2d.drawString(location.getName(), x + 10, y - 10);
        }
    }
    
    private void drawSelectedLocations(Graphics2D g2d) {
        // 绘制选中起点
        if (selectedStartLocation != null) {
            int x = (int) selectedStartLocation.getXCoordinate().doubleValue();
            int y = (int) selectedStartLocation.getYCoordinate().doubleValue();
            
            g2d.setColor(new Color(40, 167, 69, 200)); // 绿色
            g2d.fillOval(x - 12, y - 12, 24, 24);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
            g2d.drawString("起", x - 4, y + 5);
        }
        
        // 绘制选中终点
        if (selectedEndLocation != null) {
            int x = (int) selectedEndLocation.getXCoordinate().doubleValue();
            int y = (int) selectedEndLocation.getYCoordinate().doubleValue();
            
            g2d.setColor(new Color(220, 53, 69, 200)); // 红色
            g2d.fillOval(x - 12, y - 12, 24, 24);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
            g2d.drawString("终", x - 4, y + 5);
        }
    }
    
    private void drawMousePosition(Graphics2D g2d) {
        if (mousePosition != null) {
            g2d.setColor(new Color(100, 100, 100, 150));
            g2d.setFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
            String posText = String.format("(%d, %d)", mousePosition.x, mousePosition.y);
            g2d.drawString(posText, mousePosition.x + 10, mousePosition.y - 10);
        }
    }
    
    private Color getLocationColor(Location location) {
        switch (location.getType()) {
            case BUILDING:
                return new Color(52, 152, 219); // 蓝色
            case GARDEN:
                return new Color(46, 204, 113); // 绿色
            case CAFETERIA:
                return new Color(230, 126, 34); // 橙色
            case LIBRARY:
                return new Color(155, 89, 182); // 紫色
            case SPORTS:
                return new Color(231, 76, 60);  // 红色
            case GATE:
                return new Color(241, 196, 15); // 黄色
            default:
                return new Color(149, 165, 166); // 灰色
        }
    }
    
    // Setter方法
    public void setLocations(List<Location> locations) {
        this.locations = locations != null ? locations : new ArrayList<>();
        repaint();
    }
    
    public void setPathLocations(List<Location> pathLocations) {
        this.pathLocations = pathLocations != null ? pathLocations : new ArrayList<>();
        repaint();
    }
    
    public void setSelectedStartLocation(Location location) {
        this.selectedStartLocation = location;
        repaint();
    }
    
    public void setSelectedEndLocation(Location location) {
        this.selectedEndLocation = location;
        repaint();
    }
    
    public void setMousePosition(int x, int y) {
        this.mousePosition = new Point(x, y);
        repaint();
    }
    
    public void clearPath() {
        this.pathLocations.clear();
        repaint();
    }
    
    // Getter方法
    public List<Location> getLocations() {
        return locations;
    }
    
    public Location getSelectedStartLocation() {
        return selectedStartLocation;
    }
    
    public Location getSelectedEndLocation() {
        return selectedEndLocation;
    }
}