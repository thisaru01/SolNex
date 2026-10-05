/*
 * ------------------------------------------------------------------
 * File Name: ReservationsController.cs
 * Author: Kavindi P.D.I
 * Student ID: IT23234666
 * Date: 2026-09-25
 * Description: Controller for handling Reservations-related HTTP requests.
 * ------------------------------------------------------------------
 */

using SolNex.Api.DTOs.Reservations;
using SolNex.Api.Services.Reservations;
using SolNex.Api.Repositories.Reservations;
using Microsoft.AspNetCore.Mvc;
using SolNex.Api.DTOs;
using SolNex.Api.Services;
using Microsoft.AspNetCore.Authorization;
using System.Security.Claims;

namespace SolNex.Api.Controllers.Reservations;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class ReservationsController : ControllerBase
{
    private readonly IEnergyReservationService _reservationService;

    // Initializes a new instance of the ReservationsController with the specified service.
    public ReservationsController(IEnergyReservationService reservationService)
    {
        _reservationService = reservationService;
    }

    [HttpPost]
    // Creates a new energy reservation based on the provided details.
    public async Task<ActionResult<ReservationDto>> CreateReservation([FromBody] CreateReservationDto createDto)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ModelState);
        }

        try
        {
            var createdReservation = await _reservationService.CreateReservationAsync(createDto);
            
            await PopulateSlotDetailsAsync(createdReservation);

            return CreatedAtAction(nameof(GetReservationById), new { id = createdReservation.Id }, createdReservation);
        }
        catch (InvalidOperationException ex) when (ex.Message == "You already have an active reservation for this slot on the selected date.")
        {
            var slotService = (IEnergyBookingSlotService?)HttpContext.RequestServices.GetService(typeof(IEnergyBookingSlotService));
            var slot = slotService != null ? await slotService.GetSlotByIdAsync(createDto.SlotId) : null;

            return BadRequest(new
            {
                message = ex.Message,
                slotDetails = new
                {
                    startTime = slot?.StartTime,
                    endTime = slot?.EndTime,
                    dayOfWeek = slot?.DayOfWeek
                }
            });
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
        catch (ArgumentException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }

    [HttpGet("{id}")]
    [Authorize(Roles = "Backoffice,GridOperator,Prosumer")]
    // Retrieves a specific reservation by its unique identifier.
    public async Task<ActionResult<ReservationDto>> GetReservationById(string id)
    {
        var reservation = await _reservationService.GetReservationByIdAsync(id);
        if (reservation == null)
        {
            return NotFound(new { message = $"Reservation with ID {id} not found." });
        }
        
        if (!CanAccessReservation(reservation.Nic))
        {
            return Forbid();
        }

        if (User.IsInRole("GridOperator") && reservation.Status != "Approved")
        {
            return Forbid();
        }

        await PopulateSlotDetailsAsync(reservation);
        return Ok(reservation);
    }

    [HttpGet("user/{nic}")]
    // Retrieves all energy reservations associated with a specific NIC.
    public async Task<ActionResult<IEnumerable<ReservationDto>>> GetReservationsByNic(string nic)
    {
        if (!CanAccessReservation(nic))
        {
            return Forbid();
        }

        var reservations = await _reservationService.GetReservationsByNicAsync(nic);
        
        if (User.IsInRole("GridOperator"))
        {
            reservations = reservations.Where(r => r.Status == "Approved");
        }

        var reservationsList = reservations.ToList();
        await PopulateSlotDetailsAsync(reservationsList);
        return Ok(reservationsList);
    }

    [HttpGet("pending")]
    // Retrieves all energy reservations that are currently in a pending state.
    public async Task<ActionResult<IEnumerable<ReservationDto>>> GetPendingReservations()
    {
        if (User.IsInRole("GridOperator"))
        {
            return Forbid();
        }

        var reservations = await _reservationService.GetPendingReservationsAsync();
        
        if (!User.IsInRole("Backoffice"))
        {
            var currentNic = User.FindFirstValue(ClaimTypes.NameIdentifier);
            reservations = reservations.Where(r => string.Equals(r.Nic, currentNic, StringComparison.OrdinalIgnoreCase));
        }

        var reservationsList = reservations.ToList();
        await PopulateSlotDetailsAsync(reservationsList);
        return Ok(reservationsList);
    }

    [HttpPut("{id}")]
    // Updates the status or specific details of an existing reservation.
    public async Task<IActionResult> UpdateReservation(string id, [FromBody] UpdateReservationDto updateDto)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ModelState);
        }

        var existingReservation = await _reservationService.GetReservationByIdAsync(id);
        if (existingReservation == null)
        {
            return NotFound(new { message = $"Reservation with ID {id} not found." });
        }

        if (!string.IsNullOrEmpty(updateDto.Status) && Enum.TryParse<SolNex.Api.Models.ReservationStatus>(updateDto.Status, true, out var parsedStatus))
        {
            if (parsedStatus == SolNex.Api.Models.ReservationStatus.Cancelled)
            {
                if (existingReservation.Status == "CancellationRequested")
                {
                    if (!User.IsInRole("Backoffice"))
                    {
                        return Forbid();
                    }
                }
                else
                {
                    var currentNic = User.FindFirstValue(ClaimTypes.NameIdentifier);
                    if (string.IsNullOrWhiteSpace(currentNic) || !string.Equals(currentNic, existingReservation.Nic, StringComparison.OrdinalIgnoreCase))
                    {
                        return Forbid();
                    }
                }
            }
        }

        try
        {
            var updatedReservation = await _reservationService.UpdateReservationAsync(id, updateDto);
            if (updatedReservation == null)
            {
                return NotFound(new { message = $"Reservation with ID {id} not found." });
            }
            await PopulateSlotDetailsAsync(updatedReservation);
            return Ok(new { message = "Reservation updated successfully.", reservation = updatedReservation });
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
        catch (ArgumentException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }

    [HttpPut("{id}/details")]
    // Updates the specific details (e.g., slot ID, energy amount) of a pending reservation.
    public async Task<IActionResult> UpdateReservationDetails(string id, [FromBody] UpdateReservationDetailsDto updateDto)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ModelState);
        }

        var existingReservation = await _reservationService.GetReservationByIdAsync(id);
        if (existingReservation == null)
        {
            return NotFound(new { message = $"Reservation with ID {id} not found." });
        }

        var currentNic = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (string.IsNullOrWhiteSpace(currentNic) || !string.Equals(currentNic, existingReservation.Nic, StringComparison.OrdinalIgnoreCase))
        {
            return Forbid();
        }

        try
        {
            var updatedReservation = await _reservationService.UpdateReservationDetailsAsync(id, updateDto);
            if (updatedReservation == null)
            {
                return NotFound(new { message = $"Reservation with ID {id} not found." });
            }
            await PopulateSlotDetailsAsync(updatedReservation);
            return Ok(new { message = "Reservation details updated successfully.", reservation = updatedReservation });
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
        catch (ArgumentException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }

    [HttpPost("{id}/cancel-request")]
    // Requests cancellation for an existing approved or pending reservation.
    public async Task<IActionResult> RequestCancellation(string id)
    {
        var existingReservation = await _reservationService.GetReservationByIdAsync(id);
        if (existingReservation == null)
        {
            return NotFound(new { message = $"Reservation with ID {id} not found." });
        }

        var currentNic = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (string.IsNullOrWhiteSpace(currentNic) || !string.Equals(currentNic, existingReservation.Nic, StringComparison.OrdinalIgnoreCase))
        {
            return Forbid();
        }

        try
        {
            var updatedReservation = await _reservationService.RequestCancellationAsync(id);
            if (updatedReservation == null)
            {
                return NotFound(new { message = $"Reservation with ID {id} not found." });
            }
            await PopulateSlotDetailsAsync(updatedReservation);
            return Ok(new { message = "Cancellation processed successfully.", reservation = updatedReservation });
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }

    [HttpDelete("{id}")]
    // Deletes an existing reservation by its ID if permitted.
    public async Task<IActionResult> DeleteReservation(string id)
    {
        try
        {
            var reservation = await _reservationService.GetReservationByIdAsync(id);
            if (reservation == null)
            {
                return NotFound(new { message = $"Reservation with ID {id} not found." });
            }

            var currentNic = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (string.IsNullOrWhiteSpace(currentNic) || !string.Equals(currentNic, reservation.Nic, StringComparison.OrdinalIgnoreCase))
            {
                return Forbid();
            }

            if (reservation.Status != "Rejected" && reservation.Status != "Cancelled" && reservation.Status != "Completed")
            {
                return BadRequest(new { message = "Only completed, rejected, or cancelled reservations can be deleted." });
            }

            await _reservationService.DeleteReservationAsync(id);
            return NoContent();
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }

    [HttpGet("history")]
    // Retrieves the history of all energy reservations.
    public async Task<ActionResult<IEnumerable<ReservationDto>>> GetReservationHistory()
    {
        if (User.IsInRole("GridOperator"))
        {
            return Forbid();
        }

        var reservations = await _reservationService.GetAllReservationsAsync();
        
        if (!User.IsInRole("Backoffice"))
        {
            var currentNic = User.FindFirstValue(ClaimTypes.NameIdentifier);
            reservations = reservations.Where(r => string.Equals(r.Nic, currentNic, StringComparison.OrdinalIgnoreCase));
        }

        var reservationsList = reservations.ToList();
        await PopulateSlotDetailsAsync(reservationsList);
        return Ok(reservationsList);
    }

    [HttpGet("search")]
    [Authorize(Roles = "Backoffice,GridOperator,Prosumer")]
    // Searches for reservations matching the optionally provided station ID and/or status.
    public async Task<ActionResult<IEnumerable<ReservationDto>>> SearchReservations([FromQuery] string? stationId, [FromQuery] string? status)
    {
        var reservations = await _reservationService.SearchReservationsAsync(stationId, status);
        
        if (User.IsInRole("GridOperator"))
        {
            reservations = reservations.Where(r => r.Status == "Approved");
        }
        else if (!User.IsInRole("Backoffice"))
        {
            var currentNic = User.FindFirstValue(ClaimTypes.NameIdentifier);
            reservations = reservations.Where(r => string.Equals(r.Nic, currentNic, StringComparison.OrdinalIgnoreCase));
        }

        var reservationsList = reservations.ToList();
        await PopulateSlotDetailsAsync(reservationsList);
        return Ok(reservationsList);
    }

    // Populates slot details such as start time, end time, and day of the week for a collection of reservations.
    private async Task PopulateSlotDetailsAsync(IEnumerable<ReservationDto> reservations)
    {
        var slotService = (IEnergyBookingSlotService?)HttpContext.RequestServices.GetService(typeof(IEnergyBookingSlotService));
        if (slotService != null)
        {
            var uniqueSlotIds = reservations.Select(r => r.SlotId).Distinct();
            var slotDict = new Dictionary<string, SlotDto>();
            foreach (var slotId in uniqueSlotIds)
            {
                var slot = await slotService.GetSlotByIdAsync(slotId);
                if (slot != null)
                {
                    slotDict[slotId] = slot;
                }
            }

            foreach (var reservation in reservations)
            {
                if (slotDict.TryGetValue(reservation.SlotId, out var slot))
                {
                    reservation.StartTime = slot.StartTime;
                    reservation.EndTime = slot.EndTime;
                    reservation.DayOfWeek = slot.DayOfWeek;
                }
            }
        }
    }

    // Populates slot details for a single reservation.
    private async Task PopulateSlotDetailsAsync(ReservationDto reservation)
    {
        await PopulateSlotDetailsAsync(new[] { reservation });
    }

    private bool CanAccessReservation(string nic)
    {
        if (User.IsInRole("Backoffice") || User.IsInRole("GridOperator"))
        {
            return true;
        }

        var currentNic = User.FindFirstValue(ClaimTypes.NameIdentifier);
        return !string.IsNullOrWhiteSpace(currentNic)
            && string.Equals(currentNic, nic, StringComparison.OrdinalIgnoreCase);
    }
}

