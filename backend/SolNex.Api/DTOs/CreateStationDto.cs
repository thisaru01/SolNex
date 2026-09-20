using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs;

public class CreateStationDto
{
    [Required]
    public string StationId { get; set; } = null!;

    [Required]
    public string StationName { get; set; } = null!;

    [Required]
    [Range(-90, 90)]
    public double Latitude { get; set; }

    [Required]
    [Range(-180, 180)]
    public double Longitude { get; set; }

    [Required]
    [Range(0, double.MaxValue)]
    public double CapacityKw { get; set; }

    [Required]
    [Range(0, int.MaxValue)]
    public int TotalBatterySlots { get; set; }

    public Dictionary<string, string>? Schedule { get; set; }
}
