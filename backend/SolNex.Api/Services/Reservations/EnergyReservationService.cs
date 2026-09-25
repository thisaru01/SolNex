using SolNex.Api.DTOs.Reservations;
using SolNex.Api.Services.Reservations;
using SolNex.Api.Repositories.Reservations;
using SolNex.Api.DTOs;
using SolNex.Api.Models;
using SolNex.Api.Repositories;

namespace SolNex.Api.Services.Reservations;

public class EnergyReservationService : IEnergyReservationService
{
    private readonly IEnergyReservationRepository _reservationRepository;
    private readonly IEnergyBookingSlotService _slotService;

    public EnergyReservationService(IEnergyReservationRepository reservationRepository, IEnergyBookingSlotService slotService)
    {
        _reservationRepository = reservationRepository;
        _slotService = slotService;
    }

    // Retrieves all energy reservations from the repository.
    public async Task<IEnumerable<ReservationDto>> GetAllReservationsAsync()
    {
        // Fetch all reservations from the database
        var reservations = await _reservationRepository.GetAllReservationsAsync();
        return reservations.Select(MapToDto);
    }

    // Retrieves a specific reservation by its database ID or unique reservation ID.
    public async Task<ReservationDto?> GetReservationByIdAsync(string id)
    {
        // Attempt to fetch the reservation by either internal ID or public ReservationId
        var reservation = await _reservationRepository.GetReservationByIdAsync(id)
                       ?? await _reservationRepository.GetReservationByReservationIdAsync(id);

        return reservation != null ? MapToDto(reservation) : null;
    }

    // Retrieves all energy reservations associated with a specific NIC.
    public async Task<IEnumerable<ReservationDto>> GetReservationsByNicAsync(string nic)
    {
        // Fetch reservations belonging to a particular user's NIC
        var reservations = await _reservationRepository.GetReservationsByNicAsync(nic);
        return reservations.Select(MapToDto);
    }

    // Retrieves all energy reservations that are currently in a pending state.
    public async Task<IEnumerable<ReservationDto>> GetPendingReservationsAsync()
    {
        // Fetch all reservations where the status is 'Pending'
        var reservations = await _reservationRepository.GetPendingReservationsAsync();
        return reservations.Select(MapToDto);
    }

    // Creates a new energy reservation based on the provided details.
    // Calculates the correct date and time based on the slot ID.
    public async Task<ReservationDto> CreateReservationAsync(CreateReservationDto createDto)
    {
        // Validate that the slot belongs to the requested station
        if (!createDto.SlotId.StartsWith(createDto.StationId + "_", StringComparison.OrdinalIgnoreCase))
        {
            throw new ArgumentException("The provided slot ID does not belong to the specified station.");
        }

        // Fetch the slot to ensure it exists and is currently available
        var slot = await _slotService.GetSlotByIdAsync(createDto.SlotId);
        if (slot == null)
        {
            throw new ArgumentException("The specified slot does not exist.");
        }

        if (!string.Equals(slot.SlotStatus, "Available", StringComparison.OrdinalIgnoreCase))
        {
            throw new InvalidOperationException("Reservations can only be made for available slots.");
        }

        // Extract DayOfWeek and Time from SlotId (format: StationId_DayOfWeek_Time)
        createDto.ReservationDate = CalculateReservationDate(createDto.SlotId);

        // Validate that reservation is within 7 days
        var timeDifference = createDto.ReservationDate.Date - DateTime.UtcNow.Date;
        if (timeDifference.Days < 0 || timeDifference.Days > 7)
        {
            throw new ArgumentException("Reservations must be scheduled within 7 days from today.");
        }

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

        // Save the newly created reservation to the database
        await _reservationRepository.CreateReservationAsync(reservation);
        return MapToDto(reservation);
    }

    // Updates the status or details of an existing reservation.
    // Requires at least 12 hours' notice before the reservation time.
    public async Task<ReservationDto?> UpdateReservationAsync(string id, UpdateReservationDto updateDto)
    {
        // Fetch the existing reservation to update
        var reservation = await _reservationRepository.GetReservationByIdAsync(id)
                       ?? await _reservationRepository.GetReservationByReservationIdAsync(id);

        if (reservation == null) return null;

        // Enforce 12 hours' notice for updates
        var timeUntilReservation = reservation.ReservationDate - DateTime.UtcNow;
        if (timeUntilReservation.TotalHours < 12)
        {
            throw new InvalidOperationException("Updates and cancellations require at least 12 hours' notice.");
        }

        var previousStatus = reservation.Status;
        bool updated = false;

        if (!string.IsNullOrEmpty(updateDto.SlotId) || updateDto.EnergyAmountKwh.HasValue)
        {
            bool detailsUpdated = await ApplyDetailsUpdateAsync(reservation, updateDto.SlotId, updateDto.EnergyAmountKwh);
            if (detailsUpdated) updated = true;
        }

        if (!string.IsNullOrEmpty(updateDto.Status) && Enum.TryParse<ReservationStatus>(updateDto.Status, true, out var parsedStatus))
        {
            if (parsedStatus == ReservationStatus.Approved)
            {
                if (previousStatus == ReservationStatus.CancellationRequested || previousStatus == ReservationStatus.Rejected || previousStatus == ReservationStatus.Cancelled)
                {
                    throw new InvalidOperationException($"A reservation with status {previousStatus} cannot be approved.");
                }
            }
            else if (parsedStatus == ReservationStatus.Cancelled)
            {
                if (previousStatus == ReservationStatus.Approved)
                {
                    throw new InvalidOperationException("Approved reservations cannot be directly cancelled. A cancellation request must be submitted first.");
                }
                if (previousStatus == ReservationStatus.Rejected)
                {
                    throw new InvalidOperationException("A rejected reservation cannot be cancelled.");
                }
            }
            else if (parsedStatus == ReservationStatus.Rejected)
            {
                if (previousStatus == ReservationStatus.Cancelled)
                {
                    throw new InvalidOperationException("A cancelled reservation cannot be rejected.");
                }
            }

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

            // Auto-reject other pending reservations for the same slot and date when approved
            if (reservation.Status == ReservationStatus.Approved)
            {
                // Update slot status to Reserved
                await _slotService.UpdateSlotStatusAsync(reservation.SlotId, "Reserved");

                // Find and reject overlapping pending reservations
                var pendingReservations = await _reservationRepository.GetPendingReservationsAsync();
                var overlappingReservations = pendingReservations.Where(r =>
                    r.SlotId == reservation.SlotId &&
                    r.ReservationDate.Date == reservation.ReservationDate.Date &&
                    r.Id != reservation.Id);

                foreach (var overlapping in overlappingReservations)
                {
                    overlapping.Status = ReservationStatus.Rejected;
                    overlapping.RejectedReason = "Slot already reserved by another user.";
                    overlapping.UpdatedAt = DateTime.UtcNow;
                    await _reservationRepository.UpdateReservationAsync(overlapping.Id!, overlapping);
                }
            }
            else if ((previousStatus == ReservationStatus.Approved || previousStatus == ReservationStatus.CancellationRequested) && (reservation.Status == ReservationStatus.Rejected || reservation.Status == ReservationStatus.Cancelled))
            {
                // Revert slot status to Available when an approved reservation is rejected or cancelled
                await _slotService.UpdateSlotStatusAsync(reservation.SlotId, "Available");
            }
        }

        return MapToDto(reservation);
    }

    // Deletes an existing reservation by its ID.
    // Requires at least 12 hours' notice before the reservation time.
    public async Task DeleteReservationAsync(string id)
    {
        // Fetch the reservation to ensure it exists before deleting
        var reservation = await _reservationRepository.GetReservationByIdAsync(id)
                       ?? await _reservationRepository.GetReservationByReservationIdAsync(id);

        if (reservation != null && reservation.Id != null)
        {
            // Enforce 12 hours' notice for cancellations
            var timeUntilReservation = reservation.ReservationDate - DateTime.UtcNow;
            if (timeUntilReservation.TotalHours < 12)
            {
                throw new InvalidOperationException("Updates and cancellations require at least 12 hours' notice.");
            }

            if (reservation.Status == ReservationStatus.Approved)
            {
                throw new InvalidOperationException("Approved reservations cannot be directly deleted. You must submit a cancellation request.");
            }

            await _reservationRepository.DeleteReservationAsync(reservation.Id);
        }
    }

    // Searches for reservations matching the provided station ID and/or status.
    public async Task<IEnumerable<ReservationDto>> SearchReservationsAsync(string? stationId, string? status)
    {
        // Fetch and filter reservations based on search parameters
        var reservations = await _reservationRepository.SearchReservationsAsync(stationId, status);
        return reservations.Select(MapToDto);
    }

    // Maps an EnergyReservation entity to a ReservationDto object.
    private ReservationDto MapToDto(EnergyReservation reservation)
    {
        // Map fields one by one to ensure the data format is correct for API clients
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

    public async Task<ReservationDto?> RequestCancellationAsync(string id)
    {
        var reservation = await _reservationRepository.GetReservationByIdAsync(id)
                       ?? await _reservationRepository.GetReservationByReservationIdAsync(id);

        if (reservation == null) return null;

        var timeUntilReservation = reservation.ReservationDate - DateTime.UtcNow;
        if (timeUntilReservation.TotalHours < 12)
        {
            throw new InvalidOperationException("Updates and cancellations require at least 12 hours' notice.");
        }

        if (reservation.Status == ReservationStatus.Pending)
        {
            reservation.Status = ReservationStatus.Cancelled;
        }
        else if (reservation.Status == ReservationStatus.Approved)
        {
            reservation.Status = ReservationStatus.CancellationRequested;
        }
        else
        {
            throw new InvalidOperationException($"Cannot cancel a reservation that is currently {reservation.Status}.");
        }

        reservation.UpdatedAt = DateTime.UtcNow;
        await _reservationRepository.UpdateReservationAsync(reservation.Id!, reservation);

        return MapToDto(reservation);
    }

    public async Task<ReservationDto?> UpdateReservationDetailsAsync(string id, UpdateReservationDetailsDto updateDto)
    {
        var reservation = await _reservationRepository.GetReservationByIdAsync(id)
                       ?? await _reservationRepository.GetReservationByReservationIdAsync(id);

        if (reservation == null) return null;

        var timeUntilReservation = reservation.ReservationDate - DateTime.UtcNow;
        if (timeUntilReservation.TotalHours < 12)
        {
            throw new InvalidOperationException("Updates and cancellations require at least 12 hours' notice.");
        }

        bool updated = await ApplyDetailsUpdateAsync(reservation, updateDto.SlotId, updateDto.EnergyAmountKwh);

        if (updated)
        {
            reservation.UpdatedAt = DateTime.UtcNow;
            await _reservationRepository.UpdateReservationAsync(reservation.Id!, reservation);
        }

        return MapToDto(reservation);
    }

    private async Task<bool> ApplyDetailsUpdateAsync(EnergyReservation reservation, string? newSlotId, double? newEnergyAmountKwh)
    {
        if (string.IsNullOrEmpty(newSlotId) && !newEnergyAmountKwh.HasValue)
        {
            return false;
        }

        if (reservation.Status != ReservationStatus.Pending)
        {
            throw new InvalidOperationException("Reservation details can only be updated when the status is Pending.");
        }

        bool updated = false;

        if (!string.IsNullOrWhiteSpace(newSlotId) && !string.Equals(newSlotId, reservation.SlotId, StringComparison.OrdinalIgnoreCase))
        {
            if (!newSlotId.StartsWith(reservation.StationId + "_", StringComparison.OrdinalIgnoreCase))
            {
                throw new ArgumentException("The provided slot ID does not belong to the specified station.");
            }

            var slot = await _slotService.GetSlotByIdAsync(newSlotId);
            if (slot == null)
            {
                throw new ArgumentException("The specified slot does not exist.");
            }

            if (!string.Equals(slot.SlotStatus, "Available", StringComparison.OrdinalIgnoreCase))
            {
                throw new InvalidOperationException("Reservations can only be made for available slots.");
            }

            var newReservationDate = CalculateReservationDate(newSlotId);
            var timeDifference = newReservationDate.Date - DateTime.UtcNow.Date;
            if (timeDifference.Days < 0 || timeDifference.Days > 7)
            {
                throw new ArgumentException("Reservations must be scheduled within 7 days from today.");
            }

            var userReservations = await _reservationRepository.GetReservationsByNicAsync(reservation.Nic);
            var existingReservation = userReservations.FirstOrDefault(r =>
                r.Id != reservation.Id &&
                r.SlotId == newSlotId &&
                r.ReservationDate.Date == newReservationDate.Date &&
                r.Status != ReservationStatus.Cancelled &&
                r.Status != ReservationStatus.Rejected);

            if (existingReservation != null)
            {
                throw new InvalidOperationException("You already have an active reservation for this slot on the selected date.");
            }

            reservation.SlotId = newSlotId;
            reservation.ReservationDate = newReservationDate;
            updated = true;
        }

        if (newEnergyAmountKwh.HasValue)
        {
            if (newEnergyAmountKwh.Value <= 0)
            {
                throw new ArgumentException("Energy amount must be greater than zero.");
            }

            if (reservation.EnergyAmountKwh != newEnergyAmountKwh.Value)
            {
                reservation.EnergyAmountKwh = newEnergyAmountKwh.Value;
                updated = true;
            }
        }

        return updated;
    }

    private DateTime CalculateReservationDate(string slotId)
    {
        var today = DateTime.UtcNow.Date;
        var calculatedDate = today;

        var slotParts = slotId.Split('_');
        if (slotParts.Length >= 3 && Enum.TryParse<DayOfWeek>(slotParts[1], true, out var targetDay))
        {
            int daysUntil = ((int)targetDay - (int)today.DayOfWeek + 7) % 7;
            calculatedDate = today.AddDays(daysUntil);

            var timeStr = slotParts[2];
            if (int.TryParse(timeStr, out _) && (timeStr.Length == 3 || timeStr.Length == 4))
            {
                int hour = int.Parse(timeStr.Length == 4 ? timeStr.Substring(0, 2) : timeStr.Substring(0, 1));
                int minute = int.Parse(timeStr.Substring(timeStr.Length - 2));
                calculatedDate = calculatedDate.AddHours(hour).AddMinutes(minute);
            }

            if (calculatedDate <= DateTime.UtcNow)
            {
                calculatedDate = calculatedDate.AddDays(7);
            }
        }

        return calculatedDate;
    }
}

