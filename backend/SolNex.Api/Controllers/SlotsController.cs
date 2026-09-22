using Microsoft.AspNetCore.Mvc;
using SolNex.Api.DTOs;
using SolNex.Api.Services;

namespace SolNex.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
public class SlotsController : ControllerBase
{
    private readonly IEnergyBookingSlotService _slotService;

    public SlotsController(IEnergyBookingSlotService slotService)
    {
        _slotService = slotService;
    }

    [HttpGet]
    public async Task<ActionResult<IEnumerable<SlotDto>>> GetAllSlots()
    {
        var slots = await _slotService.GetAllSlotsAsync();
        return Ok(slots);
    }

    [HttpGet("available")]
    public async Task<ActionResult<IEnumerable<SlotDto>>> GetAvailableSlots()
    {
        var slots = await _slotService.GetAvailableSlotsAsync();
        return Ok(slots);
    }

    [HttpGet("{stationId}")]
    public async Task<ActionResult<IEnumerable<SlotDto>>> GetSlotsByStationId(string stationId)
    {
        var slots = await _slotService.GetSlotsByStationIdAsync(stationId);
        return Ok(slots);
    }

    [HttpPost]
    public async Task<ActionResult<SlotDto>> CreateSlot([FromBody] CreateSlotDto createDto)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ModelState);
        }

        var createdSlot = await _slotService.CreateSlotAsync(createDto);
        return CreatedAtAction(nameof(GetAllSlots), new { id = createdSlot.Id }, createdSlot);
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteSlot(string id)
    {
        await _slotService.DeleteSlotAsync(id);
        return NoContent();
    }
}
