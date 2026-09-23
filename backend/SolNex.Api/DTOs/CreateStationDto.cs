using System.ComponentModel.DataAnnotations;
using SolNex.Api.Attributes;

namespace SolNex.Api.DTOs;

public class CreateStationDto
{
    [Required]
    [RegularExpression(@"^ST\d{3}$", ErrorMessage = "StationId must start with 'ST' followed by 3 digits (e.g. ST001)")]
    [MaxLength(10)]
    public string StationId { get; set; } = null!;

    [Required]
    [StringLength(100, MinimumLength = 3)]
    public string StationName { get; set; } = null!;

    [Required]
    [Range(-90, 90)]
    public double Latitude { get; set; }

    [Required]
    [Range(-180, 180)]
    public double Longitude { get; set; }

    [Required]
    [Range(1, 10000000, ErrorMessage = "Capacity must be between 1 and 10,000,000 kW")]
    public double CapacityKw { get; set; }

    [Required]
    [Range(1, 100000, ErrorMessage = "TotalBatterySlots must be between 1 and 100000")]
    public int TotalBatterySlots { get; set; }

    [ValidSchedule]
    public Dictionary<string, string>? Schedule { get; set; }
}
