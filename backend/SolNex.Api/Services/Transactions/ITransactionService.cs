/*
 * File: ITransactionService.cs
 * Component: Transaction Services
 * Description:
 * Declares operations for retrieving, verifying, and completing transactions.
 */

using SolNex.Api.DTOs.Transactions;

namespace SolNex.Api.Services.Transactions;

public interface ITransactionService
{
    // Retrieves transactions awaiting verification or completion.
    Task<IEnumerable<TransactionDto>>
        GetPendingAsync();

    // Retrieves completed transactions.
    Task<IEnumerable<TransactionDto>>
        GetCompletedAsync();

    // Retrieves a transaction by its database or public identifier.
    Task<TransactionDto?>
        GetByIdAsync(
            string id);

    // Verifies a transaction using its QR token and operator identifier.
    Task<TransactionDto>
        VerifyAsync(
            string qrToken,
            string operatorNic);

    // Completes a verified transaction and its linked reservation.
    Task<TransactionDto>
        CompleteAsync(
            string id,
            string operatorNic);

    // Retrieves a reservation transaction for its Prosumer.
    Task<TransactionDto?>
        GetByReservationIdAsync(
            string reservationId,
            string prosumerNic);

    // Retrieves active approved transactions for a Prosumer.
    Task<IEnumerable<TransactionDto>>
        GetActiveForProsumerAsync(
            string prosumerNic);
}