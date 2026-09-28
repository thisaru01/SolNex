using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs.Transactions;

public class RejectReservationDto
{
    [Required]
    [StringLength(
        500,
        MinimumLength = 3)]
    public string Reason
    {
        get;
        set;
    } = null!;
}