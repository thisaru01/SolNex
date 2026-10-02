/*
 * ------------------------------------------------------------------
 * File Name: StationWithDistanceDto.cs
 * Author: Rajapaksha T.M
 * Student ID: IT23235892
 * Date: 2026-10-02
 * Description: Data transfer object for station with distance information.
 * ------------------------------------------------------------------
 */

namespace SolNex.Api.DTOs.Stations;

public class StationWithDistanceDto
{
    public StationDto Station { get; set; } = null!;
    public double DistanceKm { get; set; }
}
