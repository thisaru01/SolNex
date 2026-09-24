using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs.Transactions;

public class ApproveReservationDto
{
    [Required]
    public string OperatorNic { get; set; } = null!;
}
