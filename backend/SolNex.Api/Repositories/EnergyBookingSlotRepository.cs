using MongoDB.Driver;
using SolNex.Api.Data;
using SolNex.Api.Models;
using MongoDB.Bson;

namespace SolNex.Api.Repositories;

public class EnergyBookingSlotRepository : IEnergyBookingSlotRepository
{
    private readonly IMongoCollection<EnergyBookingSlot> _slots;

    public EnergyBookingSlotRepository(MongoDbContext dbContext)
    {
        _slots = dbContext.EnergyBookingSlots;

        // Ensure indexes
        var indexOptions = new CreateIndexOptions { Unique = true };
        var indexKeys = Builders<EnergyBookingSlot>.IndexKeys.Ascending(s => s.SlotId);
        var indexModel = new CreateIndexModel<EnergyBookingSlot>(indexKeys, indexOptions);
        _slots.Indexes.CreateOne(indexModel);
    }

    public async Task<IEnumerable<EnergyBookingSlot>> GetAllSlotsAsync()
    {
        return await _slots.Find(_ => true).ToListAsync();
    }

    public async Task<IEnumerable<EnergyBookingSlot>> GetSlotsByStationIdAsync(string stationId)
    {
        return await _slots.Find(s => s.StationId == stationId).ToListAsync();
    }

    public async Task<IEnumerable<EnergyBookingSlot>> GetAvailableSlotsAsync()
    {
        return await _slots.Find(s => s.SlotStatus == SlotStatus.Available).ToListAsync();
    }

    public async Task<EnergyBookingSlot?> GetSlotByIdAsync(string id)
    {
        if (!ObjectId.TryParse(id, out _)) return null;
        return await _slots.Find(s => s.Id == id).FirstOrDefaultAsync();
    }

    public async Task<EnergyBookingSlot?> GetSlotBySlotIdAsync(string slotId)
    {
        return await _slots.Find(s => s.SlotId == slotId).FirstOrDefaultAsync();
    }

    public async Task CreateSlotAsync(EnergyBookingSlot slot)
    {
        await _slots.InsertOneAsync(slot);
    }

    public async Task UpdateSlotAsync(string id, EnergyBookingSlot slot)
    {
        await _slots.ReplaceOneAsync(s => s.Id == id, slot);
    }

    public async Task DeleteSlotAsync(string id)
    {
        await _slots.DeleteOneAsync(s => s.Id == id);
    }
}
