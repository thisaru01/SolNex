using Microsoft.AspNetCore.Mvc;
using SolNex.Api.DTOs;
using SolNex.Api.DTOs.Stations;
using SolNex.Api.Services;
using SolNex.Api.Services.Stations;

namespace SolNex.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
public class StationsController : ControllerBase
{
    private readonly IStationService _stationService;

    public StationsController(IStationService stationService)
    {
        _stationService = stationService;
    }

    [HttpGet]
    public async Task<ActionResult<IEnumerable<StationDto>>> GetAllStations()
    {
        var stations = await _stationService.GetAllStationsAsync();
        return Ok(stations);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<StationDto>> GetStationById(string id)
    {
        var station = await _stationService.GetStationByIdAsync(id);
        if (station == null)
        {
            return NotFound(new { message = $"Station with ID {id} not found." });
        }
        return Ok(station);
    }

    [HttpPost]
    public async Task<ActionResult<StationDto>> CreateStation([FromBody] CreateStationDto createDto)
    {
        try
        {
            var createdStation = await _stationService.CreateStationAsync(createDto);
            // Using string interpolation for the URI as createdStation.Id could be string?
            return CreatedAtAction(nameof(GetStationById), new { id = createdStation.Id }, createdStation);
        }
        catch (InvalidOperationException ex)
        {
            return Conflict(new { message = ex.Message });
        }
    }

    [HttpPut("{id}")]
    public async Task<IActionResult> UpdateStation(string id, [FromBody] UpdateStationDto updateDto)
    {
        var updatedStation = await _stationService.UpdateStationAsync(id, updateDto);
        if (updatedStation == null)
        {
            return NotFound(new { message = $"Station with ID {id} not found." });
        }
        return Ok(new { message = "Station updated successfully.", station = updatedStation });
    }

    [HttpPut("{id}/deactivate")]
    public async Task<IActionResult> DeactivateStation(string id)
    {
        var deactivatedStation = await _stationService.DeactivateStationAsync(id);
        if (deactivatedStation == null)
        {
            return NotFound(new { message = $"Station with ID {id} not found." });
        }
        return Ok(new { message = "Station deactivated successfully.", station = deactivatedStation });
    }

    [HttpPut("{id}/activate")]
    public async Task<IActionResult> ActivateStation(string id)
    {
        var activatedStation = await _stationService.ActivateStationAsync(id);
        if (activatedStation == null)
        {
            return NotFound(new { message = $"Station with ID {id} not found." });
        }
        return Ok(new { message = "Station activated successfully.", station = activatedStation });
    }

    [HttpGet("nearby")]
    public async Task<ActionResult<IEnumerable<StationDto>>> GetNearbyStations([FromQuery] double lat, [FromQuery] double lon, [FromQuery] double radius = 10.0)
    {
        // Default radius is 10km
        var stations = await _stationService.GetNearbyStationsAsync(lat, lon, radius);
        return Ok(stations);
    }

    [HttpGet("{id}/availability")]
    public async Task<ActionResult> GetStationAvailability(string id)
    {
        var availability = await _stationService.GetStationAvailabilityAsync(id);
        if (availability == null)
        {
            return NotFound(new { message = $"Station with ID {id} not found." });
        }
        return Ok(new { availableBatterySlots = availability });
    }

    [HttpGet("{id}/schedule")]
    public async Task<ActionResult> GetStationSchedule(string id)
    {
        var schedule = await _stationService.GetStationScheduleAsync(id);
        if (schedule == null)
        {
            return NotFound(new { message = $"Station with ID {id} not found." });
        }
        return Ok(schedule);
    }

    [HttpPut("{id}/schedule")]
    public async Task<IActionResult> UpdateStationSchedule(string id, [FromBody] UpdateScheduleDto scheduleDto)
    {
        var updatedStation = await _stationService.UpdateStationScheduleAsync(id, scheduleDto);
        if (updatedStation == null)
        {
            return NotFound(new { message = $"Station with ID {id} not found." });
        }
        return Ok(new { message = "Station schedule updated successfully.", station = updatedStation });
    }

    [HttpPut("{id}/battery-slots")]
    public async Task<IActionResult> UpdateBatterySlots(string id, [FromBody] UpdateBatterySlotsDto updateDto)
    {
        try
        {
            var updatedStation = await _stationService.UpdateBatterySlotsAsync(id, updateDto);
            if (updatedStation == null)
            {
                return NotFound(new { message = $"Station with ID {id} not found." });
            }
            return Ok(new { message = "Battery slots updated successfully.", station = updatedStation });
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }
}
