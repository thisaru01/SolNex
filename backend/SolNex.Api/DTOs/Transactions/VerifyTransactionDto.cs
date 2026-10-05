/*
 * File: VerifyTransactionDto.cs
 * Component: Transaction DTOs
 * Description:
 * Carries the QR token and operator identifier for transaction verification.
 */

using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs.Transactions;

public class VerifyTransactionDto
{
    [Required]
    public string QrToken { get; set; } = null!;

    [Required]
    public string OperatorNic { get; set; } = null!;
}