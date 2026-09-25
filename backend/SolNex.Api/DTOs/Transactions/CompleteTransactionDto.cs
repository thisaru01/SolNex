using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs.Transactions;

public class CompleteTransactionDto
{
    [Required]
    public string OperatorNic { get; set; } = null!;
}