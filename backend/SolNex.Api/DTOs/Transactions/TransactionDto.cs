namespace SolNex.Api.DTOs.Transactions;

public class TransactionDto
{
    public string? Id { get; set; }
    public string TransactionId { get; set; } = null!;
    public string ReservationId { get; set; } = null!;
    public string Nic { get; set; } = null!;
    public string StationId { get; set; } = null!;
    public double EnergyAmountKwh { get; set; }
    public string? QrToken { get; set; }
    public string Status { get; set; } = null!;
    public DateTime? VerifiedAt { get; set; }
    public DateTime? CompletedAt { get; set; }
    public string? OperatorNic { get; set; }
}