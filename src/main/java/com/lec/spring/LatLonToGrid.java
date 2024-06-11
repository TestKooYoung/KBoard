package com.lec.spring;

public class LatLonToGrid {
    private static final double RE = 6371.00877; // 지구 반경(km)
    private static final double GRID = 5.0; // 격자 간격(km)
    private static final double SLAT1 = 30.0; // 투영 위도1(degree)
    private static final double SLAT2 = 60.0; // 투영 위도2(degree)
    private static final double OLON = 126.0; // 기준점 경도(degree)
    private static final double OLAT = 38.0; // 기준점 위도(degree)
    private static final double XO = 43; // 기준점 X좌표(GRID)
    private static final double YO = 136; // 기1준점 Y좌표(GRID)

    /**
     * 위도와 경도를 기상청 격자 좌표로 변환
     */
    public static double[] latLonToGrid(double lat, double lon) {
        double re = RE / GRID;
        double slat1 = Math.toRadians(SLAT1);
        double slat2 = Math.toRadians(SLAT2);
        double olon = Math.toRadians(OLON);
        double olat = Math.toRadians(OLAT);

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);
        double ra = Math.tan(Math.PI * 0.25 + Math.toRadians(lat) * 0.5);
        ra = re * sf / Math.pow(ra, sn);
        double theta = Math.toRadians(lon) - olon;
        if (theta > Math.PI) theta -= 2.0 * Math.PI;
        if (theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;
        double x = Math.floor(ra * Math.sin(theta) + XO + 0.5);
        double y = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);

        return new double[]{x, y};
    }

    public static void main(String[] args) {
        double latitude = 37.5665; // 예: 서울시청 위도
        double longitude = 126.9780; // 예: 서울시청 경도
        double[] grid = latLonToGrid(latitude, longitude);
        System.out.println("Grid X: " + grid[0] + ", Grid Y: " + grid[1]);
    }
}