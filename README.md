# SocketEnhancements: A gear enhancement plugin for PaperMC servers

Inspired by MineTinker, SocketEnhancements allows players to "enhance" their
gear in a manner fully compatible with vanilla enchantments.

SocketEnhancements aims to be compatible with many other plugins. However, using
it with other 'gear enhancement' plugins is not recommended.

## How it works

SocketEnhancements has two core features: The ability to add "sockets" to items,
and the ability to fill those sockets with enhancements.

### Sockets

Sockets represent 'slots' in an item which can be filled with enhancements. By
default, they are applied by crafting an "orb of binding" which is then added to
the item in a crafting table.

The main limiter to how powerful an item can get is how many sockets can be
applied to it, this can be configured on a per-item basis. (See sockets.yml)

### Enhancements

Enhancements represent modifiers given to items, changing their behaviour or
buffing the player in certain situations.

By default they are applied by enchanting an item with at least one empty
socket, depending on which button in the enchanting table was pressed, a single
enhancement will be randomly chosen and applied to the item.

(As you can see, I didn't spend long on choosing a name for this plugin.)

## Configuration

SocketEnhancements is highly configurable, with options ranging from tweaking
how powerful different enhancements are, to disabling core features such as orbs
of binding entirely. It is highly recommended that you configure the plugin to
your liking before you let your players use it.

## API

All of SocketEnhancements core functionality is exposed through the
"EnhancementManager" and "EnhancedItemForge" services, with the creation
of Enhancements being as simple as creating a class which implements
ActiveEnhancement. Allowing third-party plugins to create their own methods of
adding sockets and enhancements, as well as creating their own Enhancements.

That said, please note that the SocketEnhancements API is not currently
available on any public repository. It must be installed locally in order to be
used as a dependency.

## License

SocketEnhancements is licensed under the GNU General Public License V3. See
LICENSE.md
