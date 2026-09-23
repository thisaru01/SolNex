using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs;

public class UpdateStationDto
{
    [StringLength(100, MinimumLength = 3)]
    public string? StationName { get; set; }
    
    [Range(-90, 90)]
    public double? Latitude { get; set; }

    [Range(-180, 180)]
    public double? Longitude { get; set; }

    [Range(1, 10000000, ErrorMessage = "Capacity must be between 1 and 10,000,000 kW")]
    public double? CapacityKw { get; set; }

    [Range(0, 100000, ErrorMessage = "TotalBatterySlots must be between 1 and 100000")]

    public int? TotalBatterySlots { get; set; }

    [Range(0, int.MaxValue)]
    public int? AvailableBatterySlots { get; set; }
}
