using SolNex.Api.DTOs.Reservations;
using SolNex.Api.DTOs;

namespace SolNex.Api.DTOs.Transactions;

public class ReservationDecisionDto
{
    public string Message { get; set; } = null!;
    public ReservationDto Reservation { get; set; } = null!;
    public TransactionDto? Transaction { get; set; }
}
