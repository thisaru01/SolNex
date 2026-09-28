using SolNex.Api.DTOs.Transactions;

namespace SolNex.Api.Services.Transactions;

public interface ITransactionService
{
    Task<IEnumerable<TransactionDto>>
        GetPendingAsync();

    Task<IEnumerable<TransactionDto>>
        GetCompletedAsync();

    Task<TransactionDto?>
        GetByIdAsync(
            string id);

    Task<TransactionDto>
        VerifyAsync(
            string qrToken,
            string operatorNic);

    Task<TransactionDto>
        CompleteAsync(
            string id,
            string operatorNic);

    Task<TransactionDto?>
        GetByReservationIdAsync(
            string reservationId,
            string prosumerNic);

    Task<IEnumerable<TransactionDto>>
        GetActiveForProsumerAsync(
            string prosumerNic);
}