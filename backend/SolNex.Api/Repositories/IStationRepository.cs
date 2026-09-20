using SolNex.Api.Models;

namespace SolNex.Api.Repositories;

public interface IStationRepository
{
    Task<IEnumerable<SolarStationInfo>> GetAllStationsAsync();
    Task<SolarStationInfo?> GetStationByIdAsync(string id);
    Task<SolarStationInfo?> GetStationByStationIdAsync(string stationId);
    Task CreateStationAsync(SolarStationInfo station);
    Task UpdateStationAsync(string id, SolarStationInfo station);
    Task<IEnumerable<SolarStationInfo>> GetNearbyStationsAsync(double latitude, double longitude, double radiusInKm);
}
