using SolNex.Api.DTOs.Transactions;
using SolNex.Api.Models;

using SolNex.Api.Repositories.Transactions;

namespace SolNex.Api.Services.Transactions;

public class TransactionService :
    ITransactionService
{
    private readonly
        ITransactionRepository
        _transactionRepository;

    private readonly
        IReservationApprovalRepository
        _reservationRepository;

    // Initializes the transaction workflow.
    public TransactionService(
        ITransactionRepository transactionRepository,
        IReservationApprovalRepository reservationRepository)
    {
        _transactionRepository =
            transactionRepository;

        _reservationRepository =
            reservationRepository;
    }

    // Returns pending and verified
    // active transactions.
    public async Task<
        IEnumerable<TransactionDto>>
        GetPendingAsync()
    {
        var transactions =
            await _transactionRepository
                .GetPendingAsync();

        return transactions
            .Select(
                MapToDto);
    }

    // Returns completed transactions.
    public async Task<
        IEnumerable<TransactionDto>>
        GetCompletedAsync()
    {
        var transactions =
            await _transactionRepository
                .GetCompletedAsync();

        return transactions
            .Select(
                MapToDto);
    }

    // Gets one transaction.
    public async Task<
        TransactionDto?>
        GetByIdAsync(
            string id)
    {
        var transaction =
            await _transactionRepository
                .GetByIdOrTransactionIdAsync(
                    id);

        return transaction ==
               null
            ? null
            : MapToDto(
                transaction);
    }

    // Verifies a transaction using
    // the secure scanned QR token.
    public async Task<
        TransactionDto>
        VerifyAsync(
            string qrToken,
            string operatorNic)
    {
        ValidateOperatorNic(
            operatorNic);

        if (
            string.IsNullOrWhiteSpace(
                qrToken))
        {
            throw new ArgumentException(
                "QR token is required.");
        }

        var transaction =
            await _transactionRepository
                .GetByQrTokenAsync(
                    qrToken.Trim())
            ?? throw new KeyNotFoundException(
                "No transaction matches the scanned QR code.");

        if (
            transaction.Status ==
            TransactionStatus.Completed)
        {
            throw new InvalidOperationException(
                "This energy transaction has already been completed.");
        }

        if (
            transaction.Status ==
            TransactionStatus.Failed)
        {
            throw new InvalidOperationException(
                "This energy transaction is no longer valid.");
        }

        if (
            transaction.Status ==
            TransactionStatus.Verified)
        {
            if (
                !string.IsNullOrWhiteSpace(
                    transaction.OperatorNic) &&
                !string.Equals(
                    transaction.OperatorNic,
                    operatorNic.Trim(),
                    StringComparison.OrdinalIgnoreCase))
            {
                throw new InvalidOperationException(
                    "This transaction has already been verified by another Grid Operator.");
            }

            return MapToDto(
                transaction);
        }

        transaction.Status =
            TransactionStatus.Verified;

        transaction.VerifiedAt =
            DateTime.UtcNow;

        transaction.OperatorNic =
            operatorNic.Trim();

        await _transactionRepository
            .UpdateAsync(
                transaction);

        return MapToDto(
            transaction);
    }

    // Gets one transaction belonging
    // to the logged-in Prosumer.
    public async Task<
        TransactionDto?>
        GetByReservationIdAsync(
            string reservationId,
            string prosumerNic)
    {
        if (
            string.IsNullOrWhiteSpace(
                reservationId))
        {
            throw new ArgumentException(
                "Reservation ID is required.");
        }

        if (
            string.IsNullOrWhiteSpace(
                prosumerNic))
        {
            throw new ArgumentException(
                "Prosumer NIC is required.");
        }

        var transaction =
            await _transactionRepository
                .GetByReservationIdAsync(
                    reservationId.Trim());

        if (
            transaction ==
            null)
        {
            return null;
        }

        if (
            !string.Equals(
                transaction.Nic,
                prosumerNic.Trim(),
                StringComparison.OrdinalIgnoreCase))
        {
            throw new UnauthorizedAccessException(
                "This transaction does not belong to the logged-in Prosumer.");
        }

        return MapToDto(
            transaction);
    }

    // Returns all active approved
    // transactions belonging to the
    // logged-in Prosumer.
    public async Task<
        IEnumerable<TransactionDto>>
        GetActiveForProsumerAsync(
            string prosumerNic)
    {
        if (
            string.IsNullOrWhiteSpace(
                prosumerNic))
        {
            throw new ArgumentException(
                "Prosumer NIC is required.");
        }

        var transactions =
            await _transactionRepository
                .GetActiveByProsumerNicAsync(
                    prosumerNic.Trim());

        return transactions
            .Select(
                MapToDto);
    }

    // Completes a verified transaction
    // and its linked reservation.
    public async Task<
        TransactionDto>
        CompleteAsync(
            string id,
            string operatorNic)
    {
        ValidateOperatorNic(
            operatorNic);

        var transaction =
            await _transactionRepository
                .GetByIdOrTransactionIdAsync(
                    id)
            ?? throw new KeyNotFoundException(
                $"Transaction with ID {id} was not found.");

        if (
            transaction.Status ==
            TransactionStatus.Completed)
        {
            return MapToDto(
                transaction);
        }

        if (
            transaction.Status !=
            TransactionStatus.Verified)
        {
            throw new InvalidOperationException(
                "The transaction must be verified before the energy transfer can be completed.");
        }

        if (
            !string.IsNullOrWhiteSpace(
                transaction.OperatorNic) &&
            !string.Equals(
                transaction.OperatorNic,
                operatorNic.Trim(),
                StringComparison.OrdinalIgnoreCase))
        {
            throw new InvalidOperationException(
                "The transaction must be completed by the operator who verified it.");
        }

        transaction.Status =
            TransactionStatus.Completed;

        transaction.CompletedAt =
            DateTime.UtcNow;

        transaction.OperatorNic =
            operatorNic.Trim();

        await _transactionRepository
            .UpdateAsync(
                transaction);

        var reservation =
            await _reservationRepository
                .GetByIdOrReservationIdAsync(
                    transaction
                        .ReservationId);

        if (
            reservation !=
            null)
        {
            reservation.Status =
                ReservationStatus.Completed;

            reservation.UpdatedAt =
                DateTime.UtcNow;

            await _reservationRepository
                .UpdateAsync(
                    reservation);
        }

        return MapToDto(
            transaction);
    }

    // Validates operator identifier.
    private static void ValidateOperatorNic(
        string operatorNic)
    {
        if (
            string.IsNullOrWhiteSpace(
                operatorNic))
        {
            throw new ArgumentException(
                "Operator NIC is required.");
        }
    }

    // Maps database model to API DTO.
    private static TransactionDto MapToDto(
        EnergyTransaction transaction)
    {
        return new TransactionDto
        {
            Id =
                transaction.Id,

            TransactionId =
                transaction.TransactionId,

            ReservationId =
                transaction.ReservationId,

            Nic =
                transaction.Nic,

            StationId =
                transaction.StationId,

            EnergyAmountKwh =
                transaction.EnergyAmountKwh,

            QrToken =
                transaction.QrToken,

            Status =
                transaction.Status
                    .ToString(),

            VerifiedAt =
                transaction.VerifiedAt,

            CompletedAt =
                transaction.CompletedAt,

            OperatorNic =
                transaction.OperatorNic
        };
    }
}