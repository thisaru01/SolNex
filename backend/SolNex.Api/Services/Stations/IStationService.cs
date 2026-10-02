using SolNex.Api.DTOs;
using SolNex.Api.DTOs.Stations;
using SolNex.Api.DTOs.Dashboard;

namespace SolNex.Api.Services.Stations;

public interface IStationService
{
    // Retrieves all solar stations from the database.
    Task<IEnumerable<StationDto>> GetAllStationsAsync();

    // Searches and filters solar stations.
    Task<IEnumerable<StationDto>> SearchStationsAsync(string? search, string? status);

    // Retrieves a specific solar station by its internal ID or custom StationId.
    Task<StationDto?> GetStationByIdAsync(string id);

    // Creates a new solar station.
    Task<StationDto> CreateStationAsync(CreateStationDto createDto);

    // Updates basic information for an existing solar station.
    Task<StationDto?> UpdateStationAsync(string id, UpdateStationDto updateDto);

    // Deactivates a solar station so it cannot accept new reservations.
    Task<StationDto?> DeactivateStationAsync(string id);

    // Activates a previously deactivated solar station.
    Task<StationDto?> ActivateStationAsync(string id);

    // Finds solar stations within a certain radius (in kilometers) of a given latitude and longitude.
    Task<IEnumerable<StationDto>> GetNearbyStationsAsync(double latitude, double longitude, double radiusInKm);

    // Gets the number of currently available battery slots for a specific station.
    Task<int?> GetStationAvailabilityAsync(string id);

    // Retrieves the operational schedule for a specific station.
    Task<Dictionary<string, string>?> GetStationScheduleAsync(string id);

    // Updates the operational schedule for a specific station.
    Task<StationDto?> UpdateStationScheduleAsync(string id, UpdateScheduleDto scheduleDto);

    // Updates the number of available battery slots for a specific station.
    Task<StationDto?> UpdateBatterySlotsAsync(string id, UpdateBatterySlotsDto updateDto);

    // Deletes a deactivated solar station permanently.
    Task<bool> DeleteStationAsync(string id);

    // Calculates and returns the next available custom StationId (e.g., ST031).
    Task<string> GetNextStationIdAsync();

    // Retrieves dashboard metrics and recent stations.
    Task<DashboardDto> GetDashboardDataAsync();
}

