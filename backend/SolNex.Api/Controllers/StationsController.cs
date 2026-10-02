using Microsoft.AspNetCore.Mvc;
using SolNex.Api.DTOs;
using SolNex.Api.DTOs.Stations;
using SolNex.Api.DTOs.Dashboard;
using Microsoft.AspNetCore.Authorization;
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

    // GET: api/stations
    // Returns a list of all stations in the system, optionally filtered by search query and status.
    [HttpGet]
    public async Task<ActionResult<IEnumerable<StationDto>>> GetAllStations([FromQuery] string? search = null, [FromQuery] string? status = null)
    {
        var stations = await _stationService.SearchStationsAsync(search, status);
        return Ok(stations);
    }

    // GET: api/stations/dashboard
    // Returns dashboard metrics and recent stations for the frontend.
    [HttpGet("dashboard")]
    public async Task<ActionResult<DashboardDto>> GetDashboardData()
    {
        var dashboardData = await _stationService.GetDashboardDataAsync();
        return Ok(dashboardData);
    }

    // GET: api/stations/next-id
    // Fetches the next available auto-generated station ID for the frontend.
    // Restricted to Backoffice role.
    [HttpGet("next-id")]
    [Authorize(Roles = "Backoffice")]
    public async Task<ActionResult<object>> GetNextStationId()
    {
        var nextId = await _stationService.GetNextStationIdAsync();
        return Ok(new { nextId = nextId });
    }

    // GET: api/stations/{id}
    // Retrieves a single station by its ID (either internal DB ID or custom ST001 ID).
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

    // POST: api/stations
    // Creates a new station. Only accessible by Backoffice.
    [HttpPost]
    [Authorize(Roles = "Backoffice")]
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
            // Catch domain exceptions like duplicate identifiers
            return Conflict(new { message = ex.Message });
        }
    }

    // PUT: api/stations/{id}
    // Updates the details of a specific station. Restricted to Backoffice role.
    [HttpPut("{id}")]
    [Authorize(Roles = "Backoffice")]
    public async Task<IActionResult> UpdateStation(string id, [FromBody] UpdateStationDto updateDto)
    {
        // Update the station fields using the DTO provided
        var updatedStation = await _stationService.UpdateStationAsync(id, updateDto);
        if (updatedStation == null)
        {
            // If the station does not exist, return 404
            return NotFound(new { message = $"Station with ID {id} not found." });
        }
        
        // Return 200 OK along with the updated station data
        return Ok(new { message = "Station updated successfully.", station = updatedStation });
    }

    // DELETE: api/stations/{id}
    // Deletes a station from the system permanently. Restricted to Backoffice role.
    [HttpDelete("{id}")]
    [Authorize(Roles = "Backoffice")]
    public async Task<IActionResult> DeleteStation(string id)
    {
        try
        {
            var deleted = await _stationService.DeleteStationAsync(id);
            if (!deleted)
            {
                return NotFound(new { message = $"Station with ID {id} not found." });
            }
            return NoContent();
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }

    // PUT: api/stations/{id}/deactivate
    // Deactivates a station, preventing new reservations. Restricted to Backoffice role.
    [HttpPut("{id}/deactivate")]
    [Authorize(Roles = "Backoffice")]
    public async Task<IActionResult> DeactivateStation(string id)
    {
        try
        {
            var deactivatedStation = await _stationService.DeactivateStationAsync(id);
            if (deactivatedStation == null)
            {
                return NotFound(new { message = $"Station with ID {id} not found." });
            }
            return Ok(new { message = "Station deactivated successfully.", station = deactivatedStation });
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }

    // PUT: api/stations/{id}/activate
    // Activates a previously deactivated station. Restricted to Backoffice role.
    [HttpPut("{id}/activate")]
    [Authorize(Roles = "Backoffice")]
    public async Task<IActionResult> ActivateStation(string id)
    {
        var activatedStation = await _stationService.ActivateStationAsync(id);
        if (activatedStation == null)
        {
            return NotFound(new { message = $"Station with ID {id} not found." });
        }
        return Ok(new { message = "Station activated successfully.", station = activatedStation });
    }

    // GET: api/stations/closest?lat=...&lon=...&limit=...
    // Finds and returns the closest active stations to the given coordinates.
    [HttpGet("closest")]
    public async Task<ActionResult<IEnumerable<StationWithDistanceDto>>> GetClosestStations([FromQuery] double lat, [FromQuery] double lon, [FromQuery] int limit = 3)
    {
        var stations = await _stationService.GetClosestStationsAsync(lat, lon, limit);
        return Ok(stations);
    }

    // GET: api/stations/{id}/availability
    // Returns the number of available battery slots for a given station.
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

    // GET: api/stations/{id}/schedule
    // Returns the operational schedule for a specific station.
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

    // PUT: api/stations/{id}/schedule
    // Updates the operational schedule of a station. Restricted to Backoffice role.
    [HttpPut("{id}/schedule")]
    [Authorize(Roles = "Backoffice")]
    public async Task<IActionResult> UpdateStationSchedule(string id, [FromBody] UpdateScheduleDto scheduleDto)
    {
        var updatedStation = await _stationService.UpdateStationScheduleAsync(id, scheduleDto);
        if (updatedStation == null)
        {
            return NotFound(new { message = $"Station with ID {id} not found." });
        }
        return Ok(new { message = "Station schedule updated successfully.", station = updatedStation });
    }

    // PUT: api/stations/{id}/battery-slots
    // Updates the available battery slots for a given station. Restricted to Backoffice role.
    [HttpPut("{id}/battery-slots")]
    [Authorize(Roles = "Backoffice")]
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
