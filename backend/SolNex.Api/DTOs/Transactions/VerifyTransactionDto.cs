using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs.Transactions;

public class VerifyTransactionDto
{
    [Required]
    public string QrToken { get; set; } = null!;

    [Required]
    public string OperatorNic { get; set; } = null!;
}