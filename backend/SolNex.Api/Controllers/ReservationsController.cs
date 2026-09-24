using Microsoft.AspNetCore.Mvc;
using SolNex.Api.DTOs;
using SolNex.Api.Services;

namespace SolNex.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ReservationsController : ControllerBase
{
    private readonly IEnergyReservationService _reservationService;

    public ReservationsController(IEnergyReservationService reservationService)
    {
        _reservationService = reservationService;
    }

    [HttpPost]
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
    public async Task<ActionResult<ReservationDto>> GetReservationById(string id)
    {
        var reservation = await _reservationService.GetReservationByIdAsync(id);
        if (reservation == null)
        {
            return NotFound(new { message = $"Reservation with ID {id} not found." });
        }
        await PopulateSlotDetailsAsync(reservation);
        return Ok(reservation);
    }

    [HttpGet("user/{nic}")]
    public async Task<ActionResult<IEnumerable<ReservationDto>>> GetReservationsByNic(string nic)
    {
        var reservations = await _reservationService.GetReservationsByNicAsync(nic);
        var reservationsList = reservations.ToList();
        await PopulateSlotDetailsAsync(reservationsList);
        return Ok(reservationsList);
    }

    [HttpGet("pending")]
    public async Task<ActionResult<IEnumerable<ReservationDto>>> GetPendingReservations()
    {
        var reservations = await _reservationService.GetPendingReservationsAsync();
        var reservationsList = reservations.ToList();
        await PopulateSlotDetailsAsync(reservationsList);
        return Ok(reservationsList);
    }

    [HttpPut("{id}")]
    public async Task<IActionResult> UpdateReservation(string id, [FromBody] UpdateReservationDto updateDto)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ModelState);
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
    }

    [HttpPost("{id}/cancel-request")]
    public async Task<IActionResult> RequestCancellation(string id)
    {
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
    public async Task<IActionResult> DeleteReservation(string id)
    {
        try
        {
            var reservation = await _reservationService.GetReservationByIdAsync(id);
            if (reservation == null)
            {
                return NotFound(new { message = $"Reservation with ID {id} not found." });
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
    public async Task<ActionResult<IEnumerable<ReservationDto>>> GetReservationHistory()
    {
        var reservations = await _reservationService.GetAllReservationsAsync();
        var reservationsList = reservations.ToList();
        await PopulateSlotDetailsAsync(reservationsList);
        return Ok(reservationsList);
    }

    [HttpGet("search")]
    public async Task<ActionResult<IEnumerable<ReservationDto>>> SearchReservations([FromQuery] string? stationId, [FromQuery] string? status)
    {
        var reservations = await _reservationService.SearchReservationsAsync(stationId, status);
        var reservationsList = reservations.ToList();
        await PopulateSlotDetailsAsync(reservationsList);
        return Ok(reservationsList);
    }

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

    private async Task PopulateSlotDetailsAsync(ReservationDto reservation)
    {
        await PopulateSlotDetailsAsync(new[] { reservation });
    }
}
