using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs.Stations;

public class UpdateBatterySlotsDto
{
    [Required]
    [Range(0, int.MaxValue)]
    public int AvailableBatterySlots { get; set; }
}
