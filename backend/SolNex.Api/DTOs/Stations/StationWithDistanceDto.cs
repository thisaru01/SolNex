namespace SolNex.Api.DTOs.Stations;

public class StationWithDistanceDto
{
    public StationDto Station { get; set; } = null!;
    public double DistanceKm { get; set; }
}
