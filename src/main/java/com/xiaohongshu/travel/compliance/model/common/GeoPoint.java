package com.xiaohongshu.travel.compliance.model.common;

/**
 * 地理坐标点（经纬度）。
 *
 * @param latitude  纬度
 * @param longitude 经度
 */
public record GeoPoint(double latitude, double longitude) {
}
