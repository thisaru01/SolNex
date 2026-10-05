/*
 * File: CompleteTransactionDto.cs
 * Component: Transaction DTOs
 * Description:
 * Carries the operator identifier required to complete a transaction.
 */

using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs.Transactions;

public class CompleteTransactionDto
{
    [Required]
    public string OperatorNic { get; set; } = null!;
}