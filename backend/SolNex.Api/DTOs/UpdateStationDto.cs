using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs;

public class UpdateStationDto
{
    public string? StationName { get; set; }
    
    [Range(-90, 90)]
    public double? Latitude { get; set; }

    [Range(-180, 180)]
    public double? Longitude { get; set; }

    [Range(0, double.MaxValue)]
    public double? CapacityKw { get; set; }

    [Range(0, int.MaxValue)]
    public int? TotalBatterySlots { get; set; }

    [Range(0, int.MaxValue)]
    public int? AvailableBatterySlots { get; set; }
}
