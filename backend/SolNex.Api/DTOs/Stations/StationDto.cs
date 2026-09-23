namespace SolNex.Api.DTOs.Stations;

public class StationDto
{
    public string? Id { get; set; }
    public string StationId { get; set; } = null!;
    public string StationName { get; set; } = null!;
    public double Latitude { get; set; }
    public double Longitude { get; set; }
    public double CapacityKw { get; set; }
    public int TotalBatterySlots { get; set; }
    public int AvailableBatterySlots { get; set; }
    public string Status { get; set; } = null!;
    public Dictionary<string, string>? Schedule { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }
}
