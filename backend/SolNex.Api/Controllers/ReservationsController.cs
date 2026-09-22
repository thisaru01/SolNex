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

        var createdReservation = await _reservationService.CreateReservationAsync(createDto);
        return CreatedAtAction(nameof(GetReservationById), new { id = createdReservation.Id }, createdReservation);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<ReservationDto>> GetReservationById(string id)
    {
        var reservation = await _reservationService.GetReservationByIdAsync(id);
        if (reservation == null)
        {
            return NotFound(new { message = $"Reservation with ID {id} not found." });
        }
        return Ok(reservation);
    }

    [HttpGet("user/{nic}")]
    public async Task<ActionResult<IEnumerable<ReservationDto>>> GetReservationsByNic(string nic)
    {
        var reservations = await _reservationService.GetReservationsByNicAsync(nic);
        return Ok(reservations);
    }

    [HttpGet("pending")]
    public async Task<ActionResult<IEnumerable<ReservationDto>>> GetPendingReservations()
    {
        var reservations = await _reservationService.GetPendingReservationsAsync();
        return Ok(reservations);
    }

    [HttpPut("{id}")]
    public async Task<IActionResult> UpdateReservation(string id, [FromBody] UpdateReservationDto updateDto)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ModelState);
        }

        var updatedReservation = await _reservationService.UpdateReservationAsync(id, updateDto);
        if (updatedReservation == null)
        {
            return NotFound(new { message = $"Reservation with ID {id} not found." });
        }
        return Ok(new { message = "Reservation updated successfully.", reservation = updatedReservation });
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteReservation(string id)
    {
        var reservation = await _reservationService.GetReservationByIdAsync(id);
        if (reservation == null)
        {
            return NotFound(new { message = $"Reservation with ID {id} not found." });
        }

        await _reservationService.DeleteReservationAsync(id);
        return NoContent();
    }

    [HttpGet("history")]
    public async Task<ActionResult<IEnumerable<ReservationDto>>> GetReservationHistory()
    {
        var reservations = await _reservationService.GetAllReservationsAsync();
        return Ok(reservations);
    }

    [HttpGet("search")]
    public async Task<ActionResult<IEnumerable<ReservationDto>>> SearchReservations([FromQuery] string? stationId, [FromQuery] string? status)
    {
        var reservations = await _reservationService.SearchReservationsAsync(stationId, status);
        return Ok(reservations);
    }
}
