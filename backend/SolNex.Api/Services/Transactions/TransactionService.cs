using SolNex.Api.DTOs.Transactions;
using SolNex.Api.Models;
using SolNex.Api.Repositories.Transactions;

namespace SolNex.Api.Services.Transactions;

public class TransactionService : ITransactionService
{
    private readonly ITransactionRepository _transactionRepository;
    private readonly IReservationApprovalRepository _reservationRepository;

    // Injects transaction and reservation repositories used by the transfer workflow.
    public TransactionService(
        ITransactionRepository transactionRepository,
        IReservationApprovalRepository reservationRepository)
    {
        _transactionRepository = transactionRepository;
        _reservationRepository = reservationRepository;
    }

    // Returns all active transfers awaiting verification or completion.
    public async Task<IEnumerable<TransactionDto>> GetPendingAsync()
    {
        var transactions = await _transactionRepository.GetPendingAsync();
        return transactions.Select(MapToDto);
    }

    // Returns all transfers that have been finalised successfully.
    public async Task<IEnumerable<TransactionDto>> GetCompletedAsync()
    {
        var transactions = await _transactionRepository.GetCompletedAsync();
        return transactions.Select(MapToDto);
    }

    // Retrieves one transaction using either internal ID or public transaction ID.
    public async Task<TransactionDto?> GetByIdAsync(string id)
    {
        var transaction = await _transactionRepository.GetByIdOrTransactionIdAsync(id);
        return transaction == null ? null : MapToDto(transaction);
    }

    // Validates a scanned QR token against the server and marks the matching transaction as verified.
    public async Task<TransactionDto> VerifyAsync(string qrToken, string operatorNic)
    {
        ValidateOperatorNic(operatorNic);
        if (string.IsNullOrWhiteSpace(qrToken))
        {
            throw new ArgumentException("QR token is required.");
        }

        var transaction = await _transactionRepository.GetByQrTokenAsync(qrToken.Trim())
            ?? throw new KeyNotFoundException("No transaction matches the scanned QR code.");

        if (transaction.Status == TransactionStatus.Completed)
        {
            throw new InvalidOperationException("This energy transaction has already been completed.");
        }

        if (transaction.Status == TransactionStatus.Failed)
        {
            throw new InvalidOperationException("This energy transaction is no longer valid.");
        }

        if (transaction.Status == TransactionStatus.Pending)
        {
            transaction.Status = TransactionStatus.Verified;
            transaction.VerifiedAt = DateTime.UtcNow;
            transaction.OperatorNic = operatorNic.Trim();
            await _transactionRepository.UpdateAsync(transaction);
        }

        return MapToDto(transaction);
    }

    // Completes a previously verified transfer and synchronises the linked reservation status.
    public async Task<TransactionDto> CompleteAsync(string id, string operatorNic)
    {
        ValidateOperatorNic(operatorNic);

        var transaction = await _transactionRepository.GetByIdOrTransactionIdAsync(id)
            ?? throw new KeyNotFoundException($"Transaction with ID {id} was not found.");

        if (transaction.Status == TransactionStatus.Completed)
        {
            return MapToDto(transaction);
        }

        if (transaction.Status != TransactionStatus.Verified)
        {
            throw new InvalidOperationException("The transaction must be verified before the energy transfer can be completed.");
        }

        if (!string.IsNullOrWhiteSpace(transaction.OperatorNic) &&
            !string.Equals(transaction.OperatorNic, operatorNic.Trim(), StringComparison.OrdinalIgnoreCase))
        {
            throw new InvalidOperationException("The transaction must be completed by the operator who verified it.");
        }

        transaction.Status = TransactionStatus.Completed;
        transaction.CompletedAt = DateTime.UtcNow;
        transaction.OperatorNic = operatorNic.Trim();
        await _transactionRepository.UpdateAsync(transaction);

        var reservation = await _reservationRepository.GetByIdOrReservationIdAsync(transaction.ReservationId);
        if (reservation != null)
        {
            reservation.Status = ReservationStatus.Completed;
            reservation.UpdatedAt = DateTime.UtcNow;
            await _reservationRepository.UpdateAsync(reservation);
        }

        return MapToDto(transaction);
    }

    // Rejects blank operator identifiers before changing any transaction state.
    private static void ValidateOperatorNic(string operatorNic)
    {
        if (string.IsNullOrWhiteSpace(operatorNic))
        {
            throw new ArgumentException("Operator NIC is required.");
        }
    }

    // Maps a transaction entity into the DTO returned to web and Android clients.
    private static TransactionDto MapToDto(EnergyTransaction transaction)
    {
        return new TransactionDto
        {
            Id = transaction.Id,
            TransactionId = transaction.TransactionId,
            ReservationId = transaction.ReservationId,
            Nic = transaction.Nic,
            StationId = transaction.StationId,
            EnergyAmountKwh = transaction.EnergyAmountKwh,
            QrToken = transaction.QrToken,
            Status = transaction.Status.ToString(),
            VerifiedAt = transaction.VerifiedAt,
            CompletedAt = transaction.CompletedAt,
            OperatorNic = transaction.OperatorNic
        };
    }
}