/*
 * File: ReservationApprovalService.cs
 * Component: Transaction Services
 * Description:
 * Implements reservation approval, rejection, and transaction creation workflows.
 */

using System.Security.Cryptography;

using SolNex.Api.DTOs.Reservations;
using SolNex.Api.DTOs.Transactions;

using SolNex.Api.Models;

using SolNex.Api.Repositories.Transactions;

using SolNex.Api.Services.Reservations;

namespace SolNex.Api.Services.Transactions;

public class ReservationApprovalService :
    IReservationApprovalService
{
    private readonly
        IReservationApprovalRepository
        _reservationRepository;

    private readonly
        ITransactionRepository
        _transactionRepository;

    private readonly
        IEnergyBookingSlotService
        _slotService;

    public ReservationApprovalService(
        IReservationApprovalRepository reservationRepository,
        ITransactionRepository transactionRepository,
        IEnergyBookingSlotService slotService)
    {
        // Store dependencies used by the reservation decision workflow.
        _reservationRepository =
            reservationRepository;

        _transactionRepository =
            transactionRepository;

        _slotService =
            slotService;
    }

    public async Task<ReservationDecisionDto>
        ApproveAsync(
            string reservationId,
            string approverNic)
    {
        // Validate the authenticated Backoffice identity.
        ValidateApproverNic(
            approverNic);

        var reservation =
            await _reservationRepository
                .GetByIdOrReservationIdAsync(
                    reservationId)
            ?? throw new KeyNotFoundException(
                $"Reservation with ID {reservationId} was not found.");

        if (
            reservation.Status !=
            ReservationStatus.Pending)
        {
            throw new InvalidOperationException(
                $"Only pending reservations can be approved. Current status: {reservation.Status}.");
        }

        /*
         * Change the reservation to Approved
         * and record the Backoffice officer.
         */
        reservation.Status =
            ReservationStatus.Approved;

        reservation.ApprovedBy =
            approverNic.Trim();

        reservation.ApprovedAt =
            DateTime.UtcNow;

        reservation.RejectedReason =
            null;

        reservation.UpdatedAt =
            DateTime.UtcNow;

        await _reservationRepository
            .UpdateAsync(
                reservation);

        /*
         * Reserve the selected energy slot.
         */
        await _slotService
            .UpdateSlotStatusAsync(
                reservation.SlotId,
                "Reserved");

        /*
         * Check whether a transaction already
         * exists for the reservation.
         */
        var transaction =
            await _transactionRepository
                .GetByReservationIdAsync(
                    reservation
                        .ReservationId);

        if (
            transaction ==
            null)
        {
            /*
             * Create the transaction that will
             * later be handled by Grid Operator.
             */
            transaction =
                new EnergyTransaction
                {
                    TransactionId =
                        $"TX_{Guid.NewGuid().ToString("N")[..12].ToUpperInvariant()}",

                    ReservationId =
                        reservation
                            .ReservationId,

                    Nic =
                        reservation
                            .Nic,

                    StationId =
                        reservation
                            .StationId,

                    EnergyAmountKwh =
                        reservation
                            .EnergyAmountKwh,

                    QrToken =
                        GenerateQrToken(),

                    Status =
                        TransactionStatus
                            .Pending
                };

            await _transactionRepository
                .CreateAsync(
                    transaction);
        }

        /*
         * Reject other pending reservations
         * using the same slot and date.
         */
        await RejectCompetingReservationsAsync(
            reservation);

        return new ReservationDecisionDto
        {
            Message =
                "Reservation approved successfully. Energy transaction created.",

            Reservation =
                MapReservation(
                    reservation),

            Transaction =
                MapTransaction(
                    transaction)
        };
    }

    public async Task<ReservationDecisionDto>
        RejectAsync(
            string reservationId,
            string approverNic,
            string reason)
    {
        // Validate Backoffice identity and the rejection reason.
        ValidateApproverNic(
            approverNic);

        if (
            string.IsNullOrWhiteSpace(
                reason) ||
            reason.Trim().Length <
                3)
        {
            throw new ArgumentException(
                "A valid rejection reason is required.");
        }

        var reservation =
            await _reservationRepository
                .GetByIdOrReservationIdAsync(
                    reservationId)
            ?? throw new KeyNotFoundException(
                $"Reservation with ID {reservationId} was not found.");

        if (
            reservation.Status !=
            ReservationStatus.Pending)
        {
            throw new InvalidOperationException(
                $"Only pending reservations can be rejected. Current status: {reservation.Status}.");
        }

        /*
         * Reject the pending reservation.
         * A rejected reservation does not create
         * an energy transaction.
         */
        reservation.Status =
            ReservationStatus
                .Rejected;

        reservation.RejectedReason =
            reason.Trim();

        reservation.UpdatedAt =
            DateTime.UtcNow;

        await _reservationRepository
            .UpdateAsync(
                reservation);

        return new ReservationDecisionDto
        {
            Message =
                "Reservation rejected successfully.",

            Reservation =
                MapReservation(
                    reservation)
        };
    }

    private async Task
        RejectCompetingReservationsAsync(
            EnergyReservation approvedReservation)
    {
        // Find pending reservations competing for the same slot and date.
        var pending =
            await _reservationRepository
                .GetPendingForSlotDateAsync(
                    approvedReservation
                        .SlotId,

                    approvedReservation
                        .ReservationDate);

        foreach (
            var reservation in
            pending.Where(
                reservation =>
                    reservation.Id !=
                    approvedReservation.Id))
        {
            reservation.Status =
                ReservationStatus
                    .Rejected;

            reservation.RejectedReason =
                "Slot already reserved by another approved reservation.";

            reservation.UpdatedAt =
                DateTime.UtcNow;

            await _reservationRepository
                .UpdateAsync(
                    reservation);
        }
    }

    private static string
        GenerateQrToken()
    {
        // Generate a cryptographically secure token for the QR code.
        var bytes =
            RandomNumberGenerator
                .GetBytes(
                    32);

        return Convert
            .ToBase64String(
                bytes)
            .TrimEnd(
                '=')
            .Replace(
                '+',
                '-')
            .Replace(
                '/',
                '_');
    }

    private static void
        ValidateApproverNic(
            string approverNic)
    {
        // Reject requests without an authenticated Backoffice identifier.
        if (
            string.IsNullOrWhiteSpace(
                approverNic))
        {
            throw new ArgumentException(
                "Backoffice user NIC is required.");
        }
    }

    private static ReservationDto
        MapReservation(
            EnergyReservation reservation)
    {
        // Convert database reservation to API DTO.
        return new ReservationDto
        {
            Id =
                reservation.Id,

            ReservationId =
                reservation
                    .ReservationId,

            Nic =
                reservation.Nic,

            StationId =
                reservation
                    .StationId,

            SlotId =
                reservation
                    .SlotId,

            ReservationDate =
                reservation
                    .ReservationDate,

            EnergyAmountKwh =
                reservation
                    .EnergyAmountKwh,

            Status =
                reservation.Status
                    .ToString(),

            ApprovedBy =
                reservation
                    .ApprovedBy,

            ApprovedAt =
                reservation
                    .ApprovedAt,

            RejectedReason =
                reservation
                    .RejectedReason,

            CreatedAt =
                reservation
                    .CreatedAt,

            UpdatedAt =
                reservation
                    .UpdatedAt
        };
    }

    private static TransactionDto
        MapTransaction(
            EnergyTransaction transaction)
    {
        // Convert generated transaction to API DTO.
        return new TransactionDto
        {
            Id =
                transaction.Id,

            TransactionId =
                transaction
                    .TransactionId,

            ReservationId =
                transaction
                    .ReservationId,

            Nic =
                transaction.Nic,

            StationId =
                transaction
                    .StationId,

            EnergyAmountKwh =
                transaction
                    .EnergyAmountKwh,

            QrToken =
                transaction
                    .QrToken,

            Status =
                transaction.Status
                    .ToString(),

            VerifiedAt =
                transaction
                    .VerifiedAt,

            CompletedAt =
                transaction
                    .CompletedAt,

            OperatorNic =
                transaction
                    .OperatorNic
        };
    }
}