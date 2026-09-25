using SolNex.Api.DTOs.Reservations;
using SolNex.Api.Services.Reservations;
using SolNex.Api.Repositories.Reservations;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using SolNex.Api.DTOs;
using SolNex.Api.Services;

namespace SolNex.Api.Controllers.Reservations;

[ApiController]
[Route("api/[controller]")]
public class SlotsController : ControllerBase
{
    private readonly IEnergyBookingSlotService _slotService;

    // Initializes the controller with the required slot service abstraction (Dependency Inversion Principle)
    public SlotsController(IEnergyBookingSlotService slotService)
    {
        _slotService = slotService;
    }

    // Retrieves all energy booking slots across all stations
    [HttpGet]
    public async Task<ActionResult<IEnumerable<SlotDto>>> GetAllSlots()
    {
        var slots = await _slotService.GetAllSlotsAsync();
        return Ok(slots);
    }

    // Retrieves only currently available energy booking slots
    [HttpGet("available")]
    public async Task<ActionResult<IEnumerable<SlotDto>>> GetAvailableSlots()
    {
        var slots = await _slotService.GetAvailableSlotsAsync();
        return Ok(slots);
    }

    // Retrieves all booking slots for a specific charging/energy station
    [HttpGet("{stationId}")]
    public async Task<ActionResult<IEnumerable<SlotDto>>> GetSlotsByStationId(string stationId)
    {
        var slots = await _slotService.GetSlotsByStationIdAsync(stationId);
        return Ok(slots);
    }

    // Validates and creates a new booking slot within the station's operating schedule
    [HttpPost]
    [Authorize(Roles = "Backoffice")]
    public async Task<ActionResult<SlotDto>> CreateSlot([FromBody] CreateSlotDto createDto)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ModelState);
        }

        var backofficerId = User.FindFirst(System.Security.Claims.ClaimTypes.NameIdentifier)?.Value;
        var backofficerName = User.FindFirst(System.Security.Claims.ClaimTypes.Name)?.Value;

        try
        {
            var createdSlot = await _slotService.CreateSlotAsync(createDto, backofficerId, backofficerName);
            return CreatedAtAction(nameof(GetAllSlots), new { id = createdSlot.Id }, createdSlot);
        }
        catch (ArgumentException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
        catch (InvalidOperationException ex)
        {
            return Conflict(new { message = ex.Message });
        }
    }

    // Updates the status of an existing energy booking slot (e.g., Available -> Reserved)
    [HttpPut("{id}/status")]
    public async Task<ActionResult<SlotDto>> UpdateSlotStatus(string id, [FromBody] UpdateSlotStatusDto updateStatusDto)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ModelState);
        }

        try
        {
            var updatedSlot = await _slotService.UpdateSlotStatusAsync(id, updateStatusDto.Status);
            if (updatedSlot == null)
            {
                return NotFound(new { message = $"Slot with ID '{id}' not found." });
            }

            return Ok(updatedSlot);
        }
        catch (ArgumentException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }

    // Direct endpoint to reserve a slot (marks status as Reserved)
    [HttpPut("{id}/reserve")]
    public async Task<ActionResult<SlotDto>> ReserveSlot(string id)
    {
        try
        {
            var updatedSlot = await _slotService.UpdateSlotStatusAsync(id, "Reserved");
            if (updatedSlot == null)
            {
                return NotFound(new { message = $"Slot with ID '{id}' not found." });
            }

            return Ok(new { message = "Slot successfully reserved.", slot = updatedSlot });
        }
        catch (ArgumentException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }

    // Removes an existing energy booking slot by its unique identifier
    [HttpDelete("{id}")]
    [Authorize(Roles = "Backoffice")]
    public async Task<IActionResult> DeleteSlot(string id)
    {
        await _slotService.DeleteSlotAsync(id);
        return Ok(new { message = $"Slot with ID '{id}' was successfully deleted." });
    }
}

