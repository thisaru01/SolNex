using SolNex.Api.DTOs;
using SolNex.Api.Models;
using SolNex.Api.Repositories;

namespace SolNex.Api.Services;

public class EnergyReservationService : IEnergyReservationService
{
    private readonly IEnergyReservationRepository _reservationRepository;

    public EnergyReservationService(IEnergyReservationRepository reservationRepository)
    {
        _reservationRepository = reservationRepository;
    }

    public async Task<IEnumerable<ReservationDto>> GetAllReservationsAsync()
    {
        var reservations = await _reservationRepository.GetAllReservationsAsync();
        return reservations.Select(MapToDto);
    }

    public async Task<ReservationDto?> GetReservationByIdAsync(string id)
    {
        var reservation = await _reservationRepository.GetReservationByIdAsync(id)
                       ?? await _reservationRepository.GetReservationByReservationIdAsync(id);

        return reservation != null ? MapToDto(reservation) : null;
    }

    public async Task<IEnumerable<ReservationDto>> GetReservationsByNicAsync(string nic)
    {
        var reservations = await _reservationRepository.GetReservationsByNicAsync(nic);
        return reservations.Select(MapToDto);
    }

    public async Task<IEnumerable<ReservationDto>> GetPendingReservationsAsync()
    {
        var reservations = await _reservationRepository.GetPendingReservationsAsync();
        return reservations.Select(MapToDto);
    }

    public async Task<ReservationDto> CreateReservationAsync(CreateReservationDto createDto)
    {
        // Prevent user from making multiple active reservations for the same slot on the same date
        var userReservations = await _reservationRepository.GetReservationsByNicAsync(createDto.Nic);
        var existingReservation = userReservations.FirstOrDefault(r => 
            r.SlotId == createDto.SlotId && 
            r.ReservationDate.Date == createDto.ReservationDate.Date &&
            r.Status != ReservationStatus.Cancelled &&
            r.Status != ReservationStatus.Rejected);

        if (existingReservation != null)
        {
            throw new InvalidOperationException("You already have an active reservation for this slot on the selected date.");
        }

        var reservationId = $"RES_{Guid.NewGuid().ToString("N").Substring(0, 8).ToUpper()}";

        var reservation = new EnergyReservation
        {
            ReservationId = reservationId,
            Nic = createDto.Nic,
            StationId = createDto.StationId,
            SlotId = createDto.SlotId,
            ReservationDate = createDto.ReservationDate,
            EnergyAmountKwh = createDto.EnergyAmountKwh,
            Status = ReservationStatus.Pending,
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow
        };

        await _reservationRepository.CreateReservationAsync(reservation);
        return MapToDto(reservation);
    }

    public async Task<ReservationDto?> UpdateReservationAsync(string id, UpdateReservationDto updateDto)
    {
        var reservation = await _reservationRepository.GetReservationByIdAsync(id)
                       ?? await _reservationRepository.GetReservationByReservationIdAsync(id);

        if (reservation == null) return null;

        bool updated = false;

        if (!string.IsNullOrEmpty(updateDto.Status) && Enum.TryParse<ReservationStatus>(updateDto.Status, true, out var parsedStatus))
        {
            reservation.Status = parsedStatus;
            updated = true;
        }

        if (updateDto.ApprovedBy != null)
        {
            reservation.ApprovedBy = updateDto.ApprovedBy;
            updated = true;
        }

        if (updateDto.ApprovedAt.HasValue)
        {
            reservation.ApprovedAt = updateDto.ApprovedAt.Value;
            updated = true;
        }

        if (updateDto.RejectedReason != null)
        {
            reservation.RejectedReason = updateDto.RejectedReason;
            updated = true;
        }

        if (updated)
        {
            reservation.UpdatedAt = DateTime.UtcNow;
            await _reservationRepository.UpdateReservationAsync(reservation.Id!, reservation);
        }

        return MapToDto(reservation);
    }

    public async Task DeleteReservationAsync(string id)
    {
        var reservation = await _reservationRepository.GetReservationByIdAsync(id)
                       ?? await _reservationRepository.GetReservationByReservationIdAsync(id);

        if (reservation != null && reservation.Id != null)
        {
            await _reservationRepository.DeleteReservationAsync(reservation.Id);
        }
    }

    public async Task<IEnumerable<ReservationDto>> SearchReservationsAsync(string? stationId, string? status)
    {
        var reservations = await _reservationRepository.SearchReservationsAsync(stationId, status);
        return reservations.Select(MapToDto);
    }

    private ReservationDto MapToDto(EnergyReservation reservation)
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
}
