using SolNex.Api.DTOs;
using SolNex.Api.DTOs.Stations;

namespace SolNex.Api.Services.Stations;

public interface IStationService
{
    Task<IEnumerable<StationDto>> GetAllStationsAsync();
    Task<StationDto?> GetStationByIdAsync(string id);
    Task<StationDto> CreateStationAsync(CreateStationDto createDto);
    Task<StationDto?> UpdateStationAsync(string id, UpdateStationDto updateDto);
    Task<StationDto?> DeactivateStationAsync(string id);
    Task<StationDto?> ActivateStationAsync(string id);
    Task<IEnumerable<StationDto>> GetNearbyStationsAsync(double latitude, double longitude, double radiusInKm);
    Task<int?> GetStationAvailabilityAsync(string id);
    Task<Dictionary<string, string>?> GetStationScheduleAsync(string id);
    Task<StationDto?> UpdateStationScheduleAsync(string id, UpdateScheduleDto scheduleDto);
    Task<StationDto?> UpdateBatterySlotsAsync(string id, UpdateBatterySlotsDto updateDto);
}
