using SolNex.Api.DTOs.Transactions;

namespace SolNex.Api.Services.Transactions;

public interface IReservationApprovalService
{
    Task<ReservationDecisionDto> ApproveAsync(string reservationId, string operatorNic);
    Task<ReservationDecisionDto> RejectAsync(string reservationId, string operatorNic, string reason);
}