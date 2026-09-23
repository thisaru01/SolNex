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
    [Range(0, double.MaxValue)]
    public double CapacityKw { get; set; }

    [Required]
    [Range(0, int.MaxValue)]
    public int TotalBatterySlots { get; set; }

    [ValidSchedule]
    public Dictionary<string, string>? Schedule { get; set; }
}
