using SolNex.Api.DTOs.Reservations;
using SolNex.Api.Services.Reservations;
using SolNex.Api.Repositories.Reservations;
using System.Security.Cryptography;
using SolNex.Api.DTOs;
using SolNex.Api.DTOs.Transactions;
using SolNex.Api.Models;
using SolNex.Api.Repositories.Transactions;
using SolNex.Api.Services;

namespace SolNex.Api.Services.Transactions;

public class ReservationApprovalService : IReservationApprovalService
{
    private readonly IReservationApprovalRepository _reservationRepository;
    private readonly ITransactionRepository _transactionRepository;
    private readonly IEnergyBookingSlotService _slotService;

    // Injects only the abstractions required for operator decision business logic.
    public ReservationApprovalService(
        IReservationApprovalRepository reservationRepository,
        ITransactionRepository transactionRepository,
        IEnergyBookingSlotService slotService)
    {
        _reservationRepository = reservationRepository;
        _transactionRepository = transactionRepository;
        _slotService = slotService;
    }

    // Approves a pending reservation, reserves its slot, and creates exactly one pending transaction.
    public async Task<ReservationDecisionDto> ApproveAsync(string reservationId, string operatorNic)
    {
        ValidateOperatorNic(operatorNic);

        var reservation = await _reservationRepository.GetByIdOrReservationIdAsync(reservationId)
            ?? throw new KeyNotFoundException($"Reservation with ID {reservationId} was not found.");

        if (reservation.Status != ReservationStatus.Pending)
        {
            throw new InvalidOperationException($"Only pending reservations can be approved. Current status: {reservation.Status}.");
        }

        reservation.Status = ReservationStatus.Approved;
        reservation.ApprovedBy = operatorNic.Trim();
        reservation.ApprovedAt = DateTime.UtcNow;
        reservation.RejectedReason = null;
        reservation.UpdatedAt = DateTime.UtcNow;
        await _reservationRepository.UpdateAsync(reservation);

        await _slotService.UpdateSlotStatusAsync(reservation.SlotId, "Reserved");

        var transaction = await _transactionRepository.GetByReservationIdAsync(reservation.ReservationId);
        if (transaction == null)
        {
            transaction = new EnergyTransaction
            {
                TransactionId = $"TX_{Guid.NewGuid().ToString("N")[..12].ToUpperInvariant()}",
                ReservationId = reservation.ReservationId,
                Nic = reservation.Nic,
                StationId = reservation.StationId,
                EnergyAmountKwh = reservation.EnergyAmountKwh,
                QrToken = GenerateQrToken(),
                Status = TransactionStatus.Pending
            };

            await _transactionRepository.CreateAsync(transaction);
        }

        await RejectCompetingReservationsAsync(reservation);

        return new ReservationDecisionDto
        {
            Message = "Reservation approved successfully. Energy transaction created.",
            Reservation = MapReservation(reservation),
            Transaction = MapTransaction(transaction)
        };
    }

    // Rejects a pending reservation and records the operator-provided rejection reason.
    public async Task<ReservationDecisionDto> RejectAsync(string reservationId, string operatorNic, string reason)
    {
        ValidateOperatorNic(operatorNic);
        if (string.IsNullOrWhiteSpace(reason) || reason.Trim().Length < 3)
        {
            throw new ArgumentException("A valid rejection reason is required.");
        }

        var reservation = await _reservationRepository.GetByIdOrReservationIdAsync(reservationId)
            ?? throw new KeyNotFoundException($"Reservation with ID {reservationId} was not found.");

        if (reservation.Status != ReservationStatus.Pending)
        {
            throw new InvalidOperationException($"Only pending reservations can be rejected. Current status: {reservation.Status}.");
        }

        reservation.Status = ReservationStatus.Rejected;
        reservation.RejectedReason = reason.Trim();
        reservation.UpdatedAt = DateTime.UtcNow;
        await _reservationRepository.UpdateAsync(reservation);

        return new ReservationDecisionDto
        {
            Message = "Reservation rejected successfully.",
            Reservation = MapReservation(reservation)
        };
    }

    // Rejects other pending reservations that compete for the same slot and day.
    private async Task RejectCompetingReservationsAsync(EnergyReservation approvedReservation)
    {
        var pending = await _reservationRepository.GetPendingForSlotDateAsync(
            approvedReservation.SlotId,
            approvedReservation.ReservationDate);

        foreach (var reservation in pending.Where(r => r.Id != approvedReservation.Id))
        {
            reservation.Status = ReservationStatus.Rejected;
            reservation.RejectedReason = "Slot already reserved by another approved reservation.";
            reservation.UpdatedAt = DateTime.UtcNow;
            await _reservationRepository.UpdateAsync(reservation);
        }
    }

    // Generates a cryptographically random URL-safe token to place inside the QR code.
    private static string GenerateQrToken()
    {
        var bytes = RandomNumberGenerator.GetBytes(32);
        return Convert.ToBase64String(bytes)
            .TrimEnd('=')
            .Replace('+', '-')
            .Replace('/', '_');
    }

    // Rejects blank operator identifiers before any reservation state is changed.
    private static void ValidateOperatorNic(string operatorNic)
    {
        if (string.IsNullOrWhiteSpace(operatorNic))
        {
            throw new ArgumentException("Operator NIC is required.");
        }
    }

    // Maps the reservation entity to the shared reservation DTO used by clients.
    private static ReservationDto MapReservation(EnergyReservation reservation)
    {
        return new ReservationDto
        {
            Id = reservation.Id,
            ReservationId = reservation.ReservationId,
            Nic = reservation.Nic,
            StationId = reservation.StationId,
            SlotId = reservation.SlotId,
            ReservationDate = reservation.ReservationDate,
            EnergyAmountKwh = reservation.EnergyAmountKwh,
            Status = reservation.Status.ToString(),
            ApprovedBy = reservation.ApprovedBy,
            ApprovedAt = reservation.ApprovedAt,
            RejectedReason = reservation.RejectedReason,
            CreatedAt = reservation.CreatedAt,
            UpdatedAt = reservation.UpdatedAt
        };
    }

    // Maps an energy transaction entity to the API response DTO.
    private static TransactionDto MapTransaction(EnergyTransaction transaction)
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
