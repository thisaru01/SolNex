/*
 * File: TransactionService.cs
 * Component: Transaction Services
 * Description:
 * Implements transaction retrieval, verification, and completion workflows.
 */

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

    public TransactionService(
        ITransactionRepository transactionRepository,
        IReservationApprovalRepository reservationRepository)
    {
        // Stores the repositories used by transaction workflows.
        _transactionRepository =
            transactionRepository;

        _reservationRepository =
            reservationRepository;
    }

    public async Task<
        IEnumerable<TransactionDto>>
        GetPendingAsync(
            string? operatorNic = null)
    {
        // Returns pending and verified active transactions.
        var transactions =
            await _transactionRepository
                .GetPendingAsync(
                    operatorNic);

        return transactions
            .Select(
                MapToDto);
    }

    public async Task<
        IEnumerable<TransactionDto>>
        GetCompletedAsync(
            string? operatorNic = null)
    {
        // Returns completed transactions.
        var transactions =
            await _transactionRepository
                .GetCompletedAsync(
                    operatorNic);

        return transactions
            .Select(
                MapToDto);
    }

    public async Task<
        IEnumerable<TransactionDto>>
        GetOperationalHistoryAsync(
            string? operatorNic = null)
    {
        var transactions =
            await _transactionRepository
                .GetOperationalHistoryAsync(
                    operatorNic);

        return transactions
            .Select(
                MapToDto)
            .OrderByDescending(
                transaction =>
                    transaction.CompletedAt ??
                    transaction.VerifiedAt);
    }

    public async Task<
        IEnumerable<TransactionDto>>
        GetScannedByOperatorAsync(
            string operatorNic)
    {
        var transactions =
            await _transactionRepository
                .GetScannedByOperatorAsync(
                    operatorNic);

        return transactions
            .Select(
                MapToDto);
    }

    public async Task<
        TransactionDto?>
        GetByIdAsync(
            string id)
    {
        // Retrieves a transaction by its database or public identifier.
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

    public async Task<
        TransactionDto>
        VerifyAsync(
            string qrToken,
            string operatorNic)
    {
        // Verifies a transaction using the scanned secure QR token.
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

    public async Task<
        TransactionDto?>
        GetByReservationIdAsync(
            string reservationId,
            string prosumerNic)
    {
        // Retrieves a reservation transaction after verifying Prosumer ownership.
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

    public async Task<
        IEnumerable<TransactionDto>>
        GetActiveForProsumerAsync(
            string prosumerNic)
    {
        // Returns active approved transactions belonging to the Prosumer.
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

    public async Task<
        TransactionDto>
        CompleteAsync(
            string id,
            string operatorNic)
    {
        // Completes a verified transaction and updates its linked reservation.
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

    private static void ValidateOperatorNic(
        string operatorNic)
    {
        // Ensures the operator identifier is present.
        if (
            string.IsNullOrWhiteSpace(
                operatorNic))
        {
            throw new ArgumentException(
                "Operator NIC is required.");
        }
    }

    private static TransactionDto MapToDto(
        EnergyTransaction transaction)
    {
        // Maps the persisted transaction model to its API DTO.
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