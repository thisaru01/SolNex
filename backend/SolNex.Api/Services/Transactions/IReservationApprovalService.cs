using SolNex.Api.DTOs.Transactions;

namespace SolNex.Api.Services.Transactions;

public interface
    IReservationApprovalService
{
    // Approves a pending reservation and creates its transaction.
    Task<ReservationDecisionDto>
        ApproveAsync(
            string reservationId,
            string approverNic);

    // Rejects a pending reservation with a reason.
    Task<ReservationDecisionDto>
        RejectAsync(
            string reservationId,
            string approverNic,
            string reason);
}